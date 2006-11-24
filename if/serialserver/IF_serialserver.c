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
#include "common/IF_common.h"
#include "common/IF_config.h"

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


/************************/
/* Func Prototypes      */
/************************/

/* None */

/************************/
/* Functions            */
/************************/

int main(int argc, char* argv[])
{
	char *dir = NULL;
	char szConfigPath[PATH_MAX];
	char szSerialDevice[PATH_MAX];
	
    if_status_t retval = IF_OK;
    
    /* Read config file */
	if ( ( dir = getenv ("HOME") ) != NULL )
	{
		sprintf ( szConfigPath, "%s/%s", dir, "windmon.cfg" );
	}
	else
	{
		sprintf ( szConfigPath, "%s", "windmon.cfg" );
	}
	
	IF_log_event(0, IF_EV_INFO, "Loading config file '%s'", szConfigPath );
	
	if ( IF_load_config ( szConfigPath ) != IF_OK )
	{
		IF_log_event(0, IF_EV_FATAL, "Could not load config file '%s'",
		             szConfigPath);
		exit(1);
	}
	
	IF_get_param_as_string_dflt("NMEASerialPort",
	                            szSerialDevice,
	                            sizeof(szSerialDevice),
	                            "/dev/ttyS0");	    

    /* Socket fd info. Index 0 is for server (listening) port */
    if_fd_info_t *socket_fds[IF_MAX_SOCKETS + 1];
    if_fd_info_t *serial_fd = NULL;
    if_fd_info_t *new_fd = NULL;
    char *szFullMsg = "Sorry. No sockets available\n";

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

    if ( ( serial_fd = IF_serial_open ( szSerialDevice,
                                        IF_BAUD_RATE,
                                        IF_DATA_BITS,
                                        IF_STOP_BITS,
                                        IF_PARITY ) ) == NULL )
    {
            IF_log_event ( 0, IF_EV_FATAL, "Could not open serial port\n" );
            exit (1);
    }

    if ( ( socket_fds[0] = IF_socket_open ( IF_SERVER_PORT ) ) == NULL )
    {
            IF_log_event ( 0, IF_EV_FATAL, "Could not open socket\n" );
            exit (1);
    }

    sPollStruct[0].fd = serial_fd->iFD;
    sPollStruct[0].events = POLLIN;
    sPollStruct[1].fd = socket_fds[0]->iFD;
    sPollStruct[1].events = POLLIN;
    iNumFds = iNumSockets + 2;
    
    while ( retval == IF_OK )
    {
        iPollCount = poll ( sPollStruct, iNumFds, -1 );
        
        if ( iPollCount < 0 )
        {
            IF_log_event_errno(0, IF_EV_FATAL, "Could not poll devices\n");
            exit(1);
        }
        else if ( iPollCount == 0 )
        {
            /* A signal or some other interrup. Poll again */
            IF_log_event(0, IF_EV_WARN,
                         "Poll returned but nothing ready\n");
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
                 IF_fd_read(serial_fd);
                 /* Check for non-empty string in ready data buffer */
                 if ( serial_fd->zReadyData[0] != '\0' )
                 {
                    /* Write data to each socket */
                    for ( i = 0; i < iNumSockets; i++ )
                    {
                        write ( sPollStruct[i+2].fd,
                                serial_fd->zReadyData,
                                strlen(serial_fd->zReadyData) );
                    }
                }
            }
            else
            {
                IF_log_event_errno(0, IF_EV_FATAL, "Error on serial I/O\n");
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
                IF_log_event_errno(0, IF_EV_FATAL, "Error on socket I/O\n");
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
                IF_log_event(0, IF_EV_FATAL,
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
                       IF_log_event(0, IF_EV_FATAL,
                                          "Socket connection closed\n");
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
                    }
                }
                else
                {
                    /* Assume an error on this socket and close */
                    IF_log_event(0, IF_EV_FATAL,
                                       "Socket connection lost\n");
                }
            }
            j++;
        }
    }
    
    exit (0);
}

