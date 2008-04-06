#include <stdlib.h>
#include <termios.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/signal.h>
#include <sys/poll.h>
#include <sys/types.h>
#include <stdarg.h>
#include <errno.h>
#include <math.h>
#include <time.h>
#include <sys/time.h>


#include "common/IF_common.h"
#include "common/IF_config.h"
#include "common/IF_dataaccess.h"

/***********************/
/* Serial Interface    */
/***********************/
#define IF_DEVICE_NAME "/dev/ttyS0"
#define IF_BAUD_RATE 4800l
#define IF_DATA_BITS 8
#define IF_STOP_BITS 1
#define IF_PARITY    0

/***********************/
/* Socket Interface    */
/***********************/
#define IF_MAX_SOCKETS 50
/* By default, tcp/ip server listens on this port */
#define IF_SERVER_PORT 2689

/***********************/
/* Recorded wind data  */
/***********************/
static int    iNumMeasurements = 0;
static float  fSpeedMin = 0.0f;
static float  fSpeedMax = 0.0f;
static double dSpeedTotal = 0.0;
static float  fAngleXComp = 0.0f;
static float  fAngleYComp = 0.0f;
static time_t iStartDTM = 0;
static time_t iEndDTM   = 0;

/* Set when timer expires trigger writing data to database */
static if_bool_t bTrigger = IF_FALSE;

/************************/
/* Func Prototypes      */
/************************/
static void handler (int sig);
static void record_msg ( if_nmea_msg_t *pMsg );
static void log_data ();

/* None */

/************************/
/* Functions            */
/************************/

int main(int argc, char* argv[])
{
	char *dir = NULL;
	char szConfigPath[PATH_MAX];
	char szSerialDevice[PATH_MAX];

	char szDB[1024];
	char szDBUser[1024];
	char szDBHost[1024];
	char szDBPassword[1024];
	
	if_nmea_msg_t msg;
	
	int iLogInterval = 0;

    struct sigaction act = {{0}};
    struct itimerval itval = {{0}};

    /*
     * Poll structure:
     * Index 0 is serial port
     * Index 1 is socket connection listener
     * Index 2 and up is for client socket connections
     */
    struct pollfd sPollStruct[IF_MAX_SOCKETS + 2];
    int iPollCount = 0;
    int iNumSockets = 0;
    int iNumFds = 0;
    int i;
    int j;
    int m;

    /* Socket fd info. Index 0 is for server (listening) port */
    if_fd_info_t *socket_fds[IF_MAX_SOCKETS + 1];
    if_fd_info_t *serial_fd = NULL;
    if_fd_info_t *new_fd = NULL;
    char *szFullMsg = "Sorry. No sockets available\n";
	

    if_status_t retval = IF_OK;
    
    /* Generate full path for config file : <home dir>/windmon.cfg */
	if ( ( dir = getenv ("HOME") ) != NULL )
	{
		sprintf ( szConfigPath, "%s/%s", dir, "windmon.cfg" );
	}
	else
	{
		sprintf ( szConfigPath, "%s", "windmon.cfg" );
	}
	
	/* And load the config file */ 
	IF_log_event(0, IF_SEV_INFO, "Loading config file '%s'", szConfigPath );
	if ( IF_load_config ( szConfigPath ) != IF_OK )
	{
		IF_log_event(0, IF_SEV_FATAL, "Could not load config file '%s'",
		             szConfigPath);
		exit(1);
	}

	/* Set up the database connection         */
	/* Part 1 - Get settings from config file */
	if ( IF_get_param_as_string("DB",
	                             szDB,
	                             sizeof(szDB)) != IF_OK
	  || IF_get_param_as_string("DBUser",
	                             szDBUser,
	                             sizeof(szDBUser)) != IF_OK
	  || IF_get_param_as_string("DBHost",
	                             szDBHost,
	                             sizeof(szDBHost)) != IF_OK
	  || IF_get_param_as_string("DBPassword",
	                             szDBPassword,
	                             sizeof(szDBPassword)) != IF_OK )
    {
		IF_log_event(0, IF_SEV_FATAL, "Failed to read database settings");
		exit(1);
    }

	/* Part 2 - Connect to the database */
	if ( IF_db_connect ( szDBHost,
                         szDBUser,
                         szDBPassword,
                         szDB ) != IF_OK )
    {
		IF_log_event(0, IF_SEV_FATAL, "Failed to connect to database");
		exit(1);
    }    	
		

	/* Set up the serial port connection for NMEA device */
	IF_get_param_as_string_dflt("NMEASerialPort",
	                            szSerialDevice,
	                            sizeof(szSerialDevice),
	                            IF_DEVICE_NAME);	    

    /* Open serial port */
    if ( ( serial_fd = IF_serial_open ( szSerialDevice,
                                        IF_BAUD_RATE,
                                        IF_DATA_BITS,
                                        IF_STOP_BITS,
                                        IF_PARITY ) ) == NULL )
    {
            IF_log_event ( 0, IF_SEV_FATAL, "Could not open serial port\n" );
            exit (1);
    }

    /* Open socket to listen for incoming TCP connections. */
    if ( ( socket_fds[0] = IF_socket_open ( IF_SERVER_PORT ) ) == NULL )
    {
        IF_log_event_errno ( 0, IF_SEV_FATAL, "Could not open socket" );
        exit (1);
    }

    /* Populate I/O polling structure for serial port */
    sPollStruct[0].fd = serial_fd->iFD;
    sPollStruct[0].events = POLLIN;
    /* Populate I/O polling structure for TCP server port */
    sPollStruct[1].fd = socket_fds[0]->iFD;
    sPollStruct[1].events = POLLIN;
    /* Increment FD counter as we now have first to FDs in polling array */
    /* Remaining slots in array will be for incoming TCP connnections to */
    /* the server.                                                       */
    iNumFds = iNumSockets + 2;
    
	/* Set up the alarm to trigger recording of data into database */
	/* First the SIGALRM handler function */
    act.sa_handler = handler;
    sigemptyset (&act.sa_mask);
    act.sa_flags = 0;

    /* First the SIGALRM handler function */
    if ( sigaction (SIGALRM, &act, NULL) != 0 )
    {
            IF_log_event_errno ( 0, IF_SEV_FATAL, "Could not set signal handler for process timer" );
            exit (1);
    }

	/* Then set a repeating alarm */	
	if ( IF_get_param_as_int("WindLogRecordIntervalSec",
	                         &iLogInterval) != IF_OK )
	{
		IF_log_event(0, IF_SEV_FATAL, "Failed to read log interval");
		exit(1);
	}
    itval.it_interval.tv_sec = iLogInterval;
    itval.it_value.tv_sec    = iLogInterval;
    if ( setitimer ( ITIMER_REAL, &itval, NULL ) != 0 )
    {
		IF_log_event_errno(0, IF_SEV_FATAL, "Failed to set process timer");
		exit(1);
    }
    	
	/* Set the measurement period start time */
	iStartDTM = time(NULL);  


	/**********************************
	 * MAIN PROCESS LOOP
	 **********************************/
    while ( retval == IF_OK )
    {
        iPollCount = poll ( sPollStruct, iNumFds, -1 );
        
        if ( iPollCount < 0 )
        {
            IF_log_event_errno(0, IF_SEV_WARN, "Could not poll devices");
        }
        else if ( iPollCount == 0 )
        {
            /* A signal or some other interrup. Poll again */
            IF_log_event(0, IF_SEV_WARN,
                         "Poll returned but nothing ready");
            continue;
        }

        if ( sPollStruct[0].revents != 0 )
        {
            /**************************
             * Data available on the serial port
             **************************/
            iPollCount--;
            if ( sPollStruct[0].revents & POLLIN )
            {
                 /* Read data from seial port */
                 do
                 {
	                 IF_fd_read(serial_fd);
	                 /* Check for non-empty string in ready data buffer */
	                 if ( serial_fd->zReadyData[0] != '\0' )
	                 {
						/* Get NMEA message and store           */
						if ( IF_parse_nmea_msg ( &msg,
						                         serial_fd->zReadyData ) == IF_OK )
						{
							record_msg ( &msg );                	
		                    /* Write data to each socket            */
		                    for ( i = 0; i < iNumSockets; i++ )
		                    {
		                        write ( sPollStruct[i+2].fd,
		                                serial_fd->zReadyData,
		                                strlen(serial_fd->zReadyData) );
		                    }
						}
	                }
                 } while ( serial_fd->zReadyData[0] != '\0' );
            }
            else
            {
                IF_log_event_errno(0, IF_SEV_FATAL, "Error on serial I/O\n");
                exit(1);
            }
        }

        if ( sPollStruct[1].revents != 0 )
        {
            /**************************
             * New connection on socket port
             **************************/
            iPollCount--;
            if ( sPollStruct[1].revents & POLLIN )
            {
                /* Open a new socket */
                new_fd = IF_socket_listen(socket_fds[0]);
                if ( new_fd != NULL )
                {
                    if ( iNumSockets >= IF_MAX_SOCKETS )
                    {
                       write ( new_fd->iFD, szFullMsg, strlen(szFullMsg) );
                       close ( new_fd->iFD );
                       free (new_fd);
                    }
                    else
                    {
                        socket_fds[iNumSockets+1] = new_fd;
                        sPollStruct[iNumFds].fd = new_fd->iFD;
                        sPollStruct[iNumFds].events = POLLIN;
                        iNumSockets++;
                        iNumFds++;
                    }
                }
            }
            else
            {
                IF_log_event_errno(0, IF_SEV_FATAL, "Error on socket I/O\n");
                exit(1);
            }
        }

        /******************************************************************
         * Remaining poll results must be incoming data from socket clients
         * Write this data to the serial interface
         ******************************************************************/
        j = 0;
        while ( iPollCount > 0 )
        {
            if ( j > iNumSockets )
            {
                IF_log_event(0, IF_SEV_FATAL,
                                   "I/O polling error. Too many events\n");
                exit(1);
            }
            else if ( sPollStruct[j+2].revents != 0 )
            {
                iPollCount--;
                if ( sPollStruct[j+2].revents & POLLIN )
                {
                    /* Read data from socket port */
                    /* Zero bytes means socket closed */
                    if ( IF_fd_read(socket_fds[j+1]) <= 0 )
                    {
                       /* Assume an error on this socket and close */
                       IF_log_event(0, IF_SEV_WARN,
                                    "Socket connection closed '%s'",
                                    socket_fds[j+1]->zDesc );
                       /* Remove socket from arrays */
                       for ( m=j; m < iNumSockets; m++ )
                       {
                           socket_fds[m+1] = socket_fds[m+2];
                           sPollStruct[m+2] = sPollStruct[m+3];
                       }
                       iNumSockets--;
                       iNumFds--;

                    }
                    /* Check for non-empty string in ready data buffer */
                    else if ( socket_fds[j+1]->zReadyData[0] != '\0' )
                    {
                        /* Write data to serial port */
                        write ( serial_fd->iFD,
                                socket_fds[j+1]->zReadyData,
                               strlen(socket_fds[j+1]->zReadyData) );
                        IF_log_event(0,
                                     IF_SEV_INFO,
                                     "Incoming message '%s' from '%s'",
                                     socket_fds[j+1]->zReadyData,
                                     socket_fds[j+1]->zDesc);
                    }
                }
                else
                {
                    /* Assume an error on this socket and close */
                    IF_log_event(0, IF_SEV_WARN,
                                       "Socket connection lost '%s'\n",
                                       socket_fds[j+1]->zDesc);
                    IF_fd_dispose ( socket_fds[j+1] );
                   /* Remove socket from arrays */
                   for ( m=j; m < iNumSockets; m++ )
                   {
                       socket_fds[m+1] = socket_fds[m+2];
                       sPollStruct[m+2] = sPollStruct[m+3];
                   }
                   iNumSockets--;
                   iNumFds--;
                }
            }
            j++;
        }
	    if ( bTrigger == IF_TRUE )
	    {
	    	log_data();
	    	bTrigger = IF_FALSE;
	    }
    } /* Main WHILE loop */
    
    exit (0);
}

static void handler (int sig)
{
	bTrigger = IF_TRUE;
}

static void record_msg ( if_nmea_msg_t *pMsg )
{
	float fTmpSpeed;
	float fTmpAngle;
	
	if ( pMsg != NULL 
	     && pMsg->bValid == IF_TRUE
	     && pMsg->bProprietary != IF_TRUE
	     && strcmp ( pMsg->szTalkerIDString, "WI" ) == 0
	     && strcmp ( pMsg->szSentenceIDString, "MWV" ) ==0 )
	 {
		fTmpAngle = atof(pMsg->aszFields[0]);
		fTmpSpeed = atof(pMsg->aszFields[2]);

        dSpeedTotal+=(double)fTmpSpeed;

        fSpeedMin = iNumMeasurements == 0 || fTmpSpeed < fSpeedMin ? fTmpSpeed :  fSpeedMin;
        fSpeedMax = fTmpSpeed > fSpeedMax ? fTmpSpeed :  fSpeedMax;

		fAngleXComp += sin(fTmpAngle * M_PI / 180.0);
		fAngleYComp += cos(fTmpAngle * M_PI / 180.0);

		iNumMeasurements++;
		
	}
}

static void log_data ()
{
	float fSpeedAve = 0.0f;
	float fAngleAve = 0.0f;

    iEndDTM = time(NULL);

	if ( iNumMeasurements > 0 )
	{
		fSpeedAve = (float) (dSpeedTotal/iNumMeasurements);
	}
	// Average angle. This is calculated as a vector average, i.e. X and Y
	// components individually.

	// Special cases
	if ( fAngleYComp == 0.0 && fAngleXComp == 0.0 )
	{
		// No average direction(unlikely!). Default to North
		fAngleAve = 0.0f;
	}
	else if ( fAngleYComp == 0.0 )
	{
		if ( fAngleXComp > 0.0 )
			fAngleAve = 90.0f;
		else
			fAngleAve = 270.0f;
	}
	else
	{
		fAngleAve = (float) (atan(fAngleXComp / fAngleYComp) * 180.0 / M_PI);
		if ( fAngleYComp < 0.0)
		{
			fAngleAve += 180.0f;
		}
		else if ( fAngleYComp > 0.0f && fAngleXComp < 0.0f )
		{
			fAngleAve += 360.0f;
		}
	}

    IF_log_event(0, IF_SEV_INFO,
                 "Measurements=%d. Speed Min=%f, Max=%f, Ave=%f. Direction Ave=%f",
                 iNumMeasurements, fSpeedMin, fSpeedMax, fSpeedAve, fAngleAve );

    IF_db_log_wind_record ( iStartDTM,
							iEndDTM,
							iNumMeasurements,
							fSpeedMin,
							fSpeedMax,
							fSpeedAve,
							fAngleAve);

	/* Reset everything */
	iNumMeasurements = 0;
	fSpeedMin = 0.0f;
	fSpeedMax = 0.0f;
	dSpeedTotal = 0.0;
	fAngleXComp = 0.0f;
	fAngleYComp = 0.0f;
	
	/* Next measurement period starts from end of this one */
	iStartDTM = iEndDTM+1;
}


