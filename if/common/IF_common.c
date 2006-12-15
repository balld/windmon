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
#include <ctype.h>
#include <time.h>
#include "IF_common.h"


#define EV_BUFFER_SIZE 1024


/* nnn.nnn.nnn.nnn\0 = 16 chars */
#define IF_MAX_IP_ADDR_LEN 16



/************************/
/* Event Logging        */
/************************/
void _IF_log_event ( int errnoFlag,
                     int errnum, if_event_sev_t sev,
                     char *fmt, va_list arg_list )
{
    char buff[EV_BUFFER_SIZE] = {'\0'};

    time_t iNow;
    struct tm *pNow = NULL;
    char szNow[EV_BUFFER_SIZE] = {'\0'};

	iNow = time(NULL);
	pNow = localtime ( &iNow );
	strftime ( szNow, sizeof(szNow), "%Y%m%d_%H:%M:%S", pNow );     
    
    sprintf ( buff, "%c[%.6d] %s ", sev, errnum, szNow );
    
    vsprintf ( buff + strlen(buff), fmt, arg_list );
    if ( errnoFlag == IF_TRUE )
    {
        sprintf ( buff + strlen(buff), "[errno=%d '%s']", errno, strerror(errno) );
        }
    if ( buff[strlen(buff)-1] != '\n' )
    {
      strcat ( buff, "\n" );
    }
    fprintf ( stderr, buff );
}

void IF_log_event ( int errnum, if_event_sev_t sev, char *fmt, ... )
{
    va_list arg_list;
    va_start(arg_list, fmt);
        _IF_log_event ( IF_FALSE, errnum, sev, fmt, arg_list );
    va_end(arg_list);
}

void IF_log_event_errno ( int errnum, if_event_sev_t sev, char *fmt, ... )
{
    va_list arg_list;
    va_start(arg_list, fmt);
        _IF_log_event ( IF_TRUE, errnum, sev, fmt, arg_list );
    va_end(arg_list);
}


/************************/
/* General FD Fnuctions */
/************************/
if_fd_info_t *IF_fd_init (if_fd_info_t *psFDInfo)
{
    if_fd_info_t *fd_ptr;
    
    if ( psFDInfo != NULL )
    {
        fd_ptr = psFDInfo;
        fd_ptr->bDynamic = IF_FALSE;
    }
    else
    {
        if ( ( fd_ptr = (if_fd_info_t *) malloc(sizeof(if_fd_info_t))) == NULL )
        {
            IF_log_event_errno(0,
                              IF_SEV_FATAL,
                              "Could not allocate space for fd data structure");
            exit(1);
        }        	
        fd_ptr->bDynamic = IF_TRUE;
    }
    fd_ptr->iFD = -1;
    fd_ptr->iInBufferIndex = 0;
    fd_ptr->zReadyData[0] = '\0';
    fd_ptr->zInBuffer[0] = '\0';
    
    return fd_ptr;
}


if_bool_t IF_fd_is_open (if_fd_info_t *psFDInfo)
{
    if_bool_t retval = IF_TRUE;

    if ( psFDInfo->iFD < 0 || fcntl ( psFDInfo->iFD, F_GETFL ) < 0 )
    {
        retval = IF_FALSE;
    }
    return retval;
}


int IF_fd_read (if_fd_info_t *psFDInfo)
{
    /* Count of total bytes read */
    int retval = 0;

    int iCount = 0;
    /* unused int i = 0; */

    int iToGo = 0;
    int iOutLen = 0;
    char *pcBuffPtr = NULL;
    /* unused char *pcStartPtr = NULL; */
    char *pcEndPtr = NULL;

    static char zTmpBuffer[IF_BUFFER_SIZE+1];

    /* Reset the ready data buffer */
    psFDInfo->zReadyData[0] = '\0';
    
    do
    {
        pcBuffPtr = &(psFDInfo->zInBuffer[psFDInfo->iInBufferIndex]);
        iToGo = IF_BUFFER_SIZE - psFDInfo->iInBufferIndex;
        iCount = read(psFDInfo->iFD,pcBuffPtr,iToGo);
        if (iCount>0)
        {
            retval += iCount;
            /* Null terminate string */
            pcBuffPtr[iCount] = '\0';

            /* Move overall buffer index */
            psFDInfo->iInBufferIndex += iCount;

            /* Find first instance of \n (if any) in new data */
            if ( ( pcEndPtr = strchr ( pcBuffPtr, '\n' ) ) != NULL )
            {
                iOutLen = (int) (((unsigned long) pcEndPtr) 
                                 - ((unsigned long) psFDInfo->zInBuffer)) + 1;
//                *pcEndPtr = '\0';
                /* Delete any carriage returns, trailing space, etc */
//                while (    --pcEndPtr >= psFDInfo->zReadyData
//                        && isspace(*pcEndPtr) )
//                {
//                	*pcEndPtr = '\0';
//                }
                strncpy(psFDInfo->zReadyData, psFDInfo->zInBuffer, iOutLen);
                psFDInfo->zReadyData[iOutLen] = '\0';
//                fprintf ( stdout, "Line : %s", psFDInfo->zReadyData );

                if ( iOutLen < psFDInfo->iInBufferIndex )
                {
                    /* Shift remaining buffer to left and reset index */
                    psFDInfo->iInBufferIndex -= iOutLen;
                    strncpy ( zTmpBuffer, &(psFDInfo->zInBuffer[iOutLen]),
                              psFDInfo->iInBufferIndex);
                    strncpy ( psFDInfo->zInBuffer, zTmpBuffer,
                              psFDInfo->iInBufferIndex );
                    psFDInfo->zInBuffer[psFDInfo->iInBufferIndex] = '\0';
                }
                else
                {
                    psFDInfo->zInBuffer[0] = '\0';
                    psFDInfo->iInBufferIndex = 0;
                }
            }
            else if ( psFDInfo->iInBufferIndex >= IF_BUFFER_SIZE )
            {
                /* Ran out of bufer space. Print what we have */
                strncpy(psFDInfo->zReadyData, psFDInfo->zInBuffer, IF_BUFFER_SIZE);
                psFDInfo->zReadyData[IF_BUFFER_SIZE] = '\0';
                psFDInfo->iInBufferIndex = 0;
                psFDInfo->zInBuffer[0] = '\0';
            }
        }
    } while (iCount > 0 && psFDInfo->zReadyData[0] == '\0');
    return retval;
}

/*************************/
/* Serial Port Functions */
/*************************/

/*
 * Function : IF_open_serial_port
 *
 * char *zDeviceName e.g. "/dev/ttys0"  Serial device name
 * long lBaudRate e.g. 9600  Default Baud Rate (110 through 38400)
 * int iDataBits  e.g. 8     Number of data bits
 * int iStopBits  e.g. 1;    Number of stop bits
 * int iParity    e.g. 0;    Parity as follows:
 *                     00 = NONE, 01 = Odd, 02 = Even, 03 = Mark, 04 = Space
 */
if_fd_info_t *IF_serial_open( char *zDeviceName,
                            long lBaudRate,
                            int iDataBits,
                            int iStopBits, int iParity)
{
    /* Serial Port Settings */
    long BAUD;
    long DATABITS;
    long STOPBITS;
    long PARITYON;
    long PARITY;
    
    /* unused struct sigaction saio; */     //definition of signal action
    struct termios newtio;    //place for new port settings for serial port

    int      fd;
    if_fd_info_t *fd_ptr;
    
    IF_log_event(0, IF_SEV_INFO, "Device=%s, Baud=%li\n",zDeviceName, lBaudRate);
    IF_log_event(0, IF_SEV_INFO, "Data Bits=%i  Stop Bits=%i  Parity=%i\n",iDataBits, iStopBits, iParity);

     switch (lBaudRate)
     {
         case 38400:
         default:
             BAUD = B38400;
             break;
         case 19200:
             BAUD  = B19200;
             break;
         case 9600:
             BAUD  = B9600;
             break;
         case 4800:
             BAUD  = B4800;
             break;
         case 2400:
             BAUD  = B2400;
             break;
         case 1800:
             BAUD  = B1800;
             break;
         case 1200:
             BAUD  = B1200;
             break;
         case 600:
             BAUD  = B600;
             break;
         case 300:
             BAUD  = B300;
             break;
         case 200:
             BAUD  = B200;
             break;
         case 150:
             BAUD  = B150;
             break;
         case 134:
             BAUD  = B134;
             break;
         case 110:
             BAUD  = B110;
             break;
         case 75:
             BAUD  = B75;
             break;
         case 50:
             BAUD  = B50;
             break;
     }  //end of switch baud_rate
     switch (iDataBits)
     {
         case 8:
         default:
             DATABITS = CS8;
             break;
         case 7:
             DATABITS = CS7;
             break;
         case 6:
             DATABITS = CS6;
             break;
         case 5:
             DATABITS = CS5;
             break;
     }  //end of switch data_bits
     switch (iStopBits)
     {
         case 1:
         default:
             STOPBITS = 0;
             break;
         case 2:
             STOPBITS = CSTOPB;
             break;
     }  //end of switch stop bits
     switch (iParity)
     {
         case 0:
         default:                              //none
             PARITYON = 0;
             PARITY = 0;
             break;
         case 1:                                //odd
             PARITYON = PARENB;
             PARITY = PARODD;
             break;
         case 2:                                //even
             PARITYON = PARENB;
             PARITY = 0;
             break;
     }  //end of switch parity
         
     // open in blocking mode - we'll poll to see if data is available.
     fd = open(zDeviceName, O_RDWR | O_NOCTTY | O_NONBLOCK);
     if (fd < 0)
     {
         IF_log_event_errno(0, IF_SEV_FATAL, "Could not open device %s\n", zDeviceName);
         exit(1);
     }

     newtio.c_cflag = BAUD | CRTSCTS | DATABITS | STOPBITS | PARITYON | PARITY | CLOCAL | CREAD;
     newtio.c_iflag = IGNPAR;
     newtio.c_oflag = 0;
     newtio.c_lflag = 0;         //ICANON;
     newtio.c_cc[VMIN]=1;
     newtio.c_cc[VTIME]=0;
     tcflush(fd, TCIFLUSH);
     tcsetattr(fd,TCSANOW,&newtio);
     
     /* Create fd structure */
     fd_ptr = IF_fd_init(NULL);
     fd_ptr->iFD = fd;

     return fd_ptr;
}



/*************************/
/* Socket      Functions */
/*************************/

if_fd_info_t *IF_socket_open (unsigned short sPortNum)
{
    /* FD Structure */
    if_fd_info_t *fd_ptr = NULL;
    /* Socket for listening on TCP/IP port */
    static int sd = 0;
    /* Server socket settings */
    static struct sockaddr_in servAddr;
    
    /* create socket */
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if(sd<0) {
        IF_log_event_errno(0, IF_SEV_FATAL, "cannot open socket ");
        exit (1);
    }
    
    
 // Allow the process to receive SIGIO
//    fcntl(sd, F_SETOWN, getpid());
 // Make the file descriptor asynchronous (the manual page says only
//  O_APPEND and O_NONBLOCK, will work with F_SETFL...)
//    fcntl(sd, F_SETFL, O_NONBLOCK | FASYNC);
    fcntl(sd, F_SETFL, O_NONBLOCK);
    
    /* bind server port */
    servAddr.sin_family = AF_INET;
    servAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servAddr.sin_port = htons(sPortNum);
    
    if(bind(sd, (struct sockaddr *) &servAddr, sizeof(servAddr))<0) {
        IF_log_event_errno(0, IF_SEV_FATAL, "cannot bind port ");
        exit (1);
    }

    if ( listen(sd,IF_SOCKET_BACKLOG) < 0 )
    {
        IF_log_event_errno ( 0, IF_SEV_FATAL, "Could not listen on port %hd\n", sPortNum );
        exit (1);
    }
    
    IF_log_event(0, IF_SEV_INFO, "Listening socket opened on port TCP %hd\n", sPortNum);
    
    fd_ptr = IF_fd_init(NULL);
    fd_ptr->iFD = sd;
    return fd_ptr;
}



if_fd_info_t *IF_socket_listen ( if_fd_info_t *psSockFD )
{
    
    if_fd_info_t *fd_ptr = NULL;
    int newSd;
    unsigned int cliLen;
    struct sockaddr_in cliAddr;
    
    IF_log_event(0, IF_SEV_INFO, "Checking for new connection "
                                "on TCP server port\n" );
    
    cliLen = sizeof(cliAddr);
    newSd = accept(psSockFD->iFD,
                   (struct sockaddr *) &cliAddr,
                   &cliLen);
    if(newSd<0) {
        IF_log_event_errno(0, IF_SEV_WARN,
                           "Failed to accept socket connection");
    }
    else
    {
        /* Make socket non-blocking */
        fcntl(newSd, F_SETFL, O_NONBLOCK);
        fd_ptr = IF_fd_init(NULL);
        fd_ptr->iFD = newSd;
        sprintf ( fd_ptr->zDesc, "%s:%hd",
                                 inet_ntoa(cliAddr.sin_addr),
                                 ntohs(cliAddr.sin_port) );
        IF_log_event ( 0,
                       IF_SEV_INFO,
                       "Got connection from '%s'",
                       fd_ptr->zDesc);
    }
            
    return fd_ptr;
}

if_status_t IF_fd_dispose (if_fd_info_t *psFDInfo)
{
    if ( IF_fd_is_open ( psFDInfo ) == IF_TRUE )
    {
    	close ( psFDInfo->iFD );
    }
    if ( psFDInfo->bDynamic == IF_TRUE )
    {
    	free ( psFDInfo );
    }
    return (IF_OK);
}

if_status_t IF_parse_nmea_msg ( if_nmea_msg_t *pMsg, char *szMsgStr )
{
	char *pCursor = NULL;
	char *pTmp    = NULL;
	char szStr[IF_NMEA_RECORD_SIZE];
	int  iFieldIndex = 0;
	
	/* Initialise the NMEA structure */
	pMsg->iNumFields=0;
	pMsg->bValid = IF_FALSE;
	pMsg->bProprietary = IF_FALSE;
    /* null fill all strings - saves us null terminating every strncpy below */
	memset( pMsg->aszFields, 0, sizeof(pMsg->aszFields));
	memset( pMsg->szManufacturerIDString, 0, sizeof(pMsg->szManufacturerIDString));
	memset( pMsg->szSentenceIDString, 0, sizeof(pMsg->szSentenceIDString));
	memset( pMsg->szTalkerIDString, 0, sizeof(pMsg->szTalkerIDString));
	memset( pMsg->szRawData, 0, sizeof(pMsg->szRawData));
	
	strncpy ( szStr, szMsgStr, sizeof(szStr));
	szStr[sizeof(szStr)-1] = '\0';

	/* Start from end of string - remove white space, /n, /r, etc */
	pTmp = szStr + strlen(szStr);
	while ( --pTmp >= szStr && isspace(*pTmp))
	{
		*pTmp='\0';
	}
		

	/* Store string data */
	strcpy ( pMsg->szRawData, szStr);
	
    /* Check for "$" in first character */
    pCursor = szStr;
    if ( strlen(szStr) <= 0 || *pCursor != '$')
    {
        IF_log_event ( 0,
                       IF_SEV_ERROR,
                       "Invalid NMEA string '%s' : Missing '$' prefix",
                       szStr);
        return IF_ERR;
    }
    pCursor++;

    /* Check "$" does not occur in remainder of message */
    /* This indicates a garbled message                 */
    if ( strchr ( pCursor, (int) '$') != NULL )
    {
        IF_log_event ( 0,
                       IF_SEV_ERROR,
                       "Garbled NMEA string '%s' : '$' prefix occurs within message",
                       szStr);
        return IF_ERR;
    }



    /* Parse the message string */
    if ( *pCursor == 'P' )
    {
        // Proprietary message. Need atleast 5 characters
        if ( strlen(szStr) < 5 )
        {
            IF_log_event ( 0,
                           IF_SEV_ERROR,
                       	   "Invalid NMEA string. '%s': Proprietary message too short",
                           szStr);
	        return IF_ERR;
        }
        pMsg->bProprietary = IF_TRUE;

		pCursor++;        
        /* Next 3 characters are manfr ID */
        strncpy ( pMsg->szManufacturerIDString, pCursor, 3 );
        pCursor+=3;
    }
    else
    {
        // Standard message. Need atleast 6 characters
        if ( strlen(szStr) < 6 )
        {
            IF_log_event ( 0,
                           IF_SEV_ERROR,
                       	   "Invalid NMEA string. '%s': Standard message too short",
                           szStr);
	        return IF_ERR;
        }
        pMsg->bProprietary = IF_FALSE;

        /* First two characters are Talker ID */
        strncpy ( pMsg->szTalkerIDString, pCursor, 2 );
        pCursor+=2;

        /* Next 3 characters are the Sentence ID */
        strncpy ( pMsg->szSentenceIDString, pCursor, 3 );
        pCursor+=3;
    }
    
    /* Assuming there is more data to come, start processing the */ 
    /* comma separated fields.                                   */
    if ( *pCursor != '\0' )
    {
        /* Check for comma immediately following message header */
        if ( *pCursor != ',' )
        {
            IF_log_event ( 0,
                           IF_SEV_ERROR,
                       	   "Invalid NMEA string. '%s': "
                       	   "No comma seperator before data fields",
                           szStr);
	        return IF_ERR;
        }
        
        /* Advance past comma */
        pCursor++;

        /* Find next comma */
        do
        {
	        pTmp = strchr(pCursor, ',');
            if ( pTmp != NULL )
            {
            	/* Replace next comma with null char */
            	*pTmp = '\0';
            	pTmp++; /* Advance one char */
            	
            }
            /* No more commas. Field extends to end of string */
            strncpy ( pMsg->aszFields[iFieldIndex],
                     pCursor,
                     sizeof(pMsg->aszFields[iFieldIndex]));
            iFieldIndex++;
            pCursor = pTmp;
        } while ( pCursor != NULL );
    }
    pMsg->iNumFields = iFieldIndex;
    pMsg->bValid = IF_TRUE;
    
    return IF_OK;
}
	
void IF_log_nmea_msg ( if_nmea_msg_t *pMsg )
{
	int i;
	
    IF_log_event ( 0, IF_SEV_INFO,
                   "NMEA message follows..." );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Raw text : '%s'",
                   pMsg->szRawData );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Valid : '%s'",
                   pMsg->bValid == IF_TRUE ? "Y" : "N" );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Proprietary : '%s'",
                   pMsg->bProprietary == IF_TRUE ? "Y" : "N" );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Mfctr ID : '%s'",
                   pMsg->szManufacturerIDString );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Talker ID : '%s'",
                   pMsg->szTalkerIDString );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Sentence ID : '%s'",
                   pMsg->szSentenceIDString );
    IF_log_event ( 0, IF_SEV_INFO,
                   "    Num fields : %d",
                   pMsg->iNumFields );
    for ( i = 0; i < pMsg->iNumFields; i++ )
    {
	    IF_log_event ( 0, IF_SEV_INFO,
	                   "    Field %2d : '%s'",
	                   i,
	                   pMsg->aszFields[i] );
    }

    IF_log_event ( 0, IF_SEV_INFO,
                   "... NMEA message ends" );
}

