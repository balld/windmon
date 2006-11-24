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

/***********************/
/* Serial Interface    */
/***********************/
#define IF_DEVICE_NAME "/dev/ttyS1"
#define IF_BAUD_RATE 4800l
#define IF_DATA_BITS 8
#define IF_STOP_BITS 1
#define IF_PARITY    0


/************************/
/* Func Prototypes      */
/************************/

/* None */

/************************/
/* Functions            */
/************************/

int main(int argc, char* argv[])
{
    char szSerialDevice[PATH_MAX] = {'\0'};
    char szMsg[1024];
    
    float angle = 0.0f;
    float adelta = 1.0f;
    
    float speed = 0.0f;
    float sdelta = 1.0f;
    
	
    /* Socket fd info. Index 0 is for server (listening) port */
    if_fd_info_t *serial_fd = NULL;


    if ( ( serial_fd = IF_serial_open ( IF_DEVICE_NAME,
                                        IF_BAUD_RATE,
                                        IF_DATA_BITS,
                                        IF_STOP_BITS,
                                        IF_PARITY ) ) == NULL )
    {
            IF_log_event ( 0, IF_EV_FATAL, "Could not open serial port\n" );
            exit (1);
    }


    while ( 1 )
    {
        /* Write data to serial port */
        sprintf ( szMsg, "$WIMWV,%.2f,R,%.2f,N,A*FF\r\n", angle, speed );
        fprintf ( stdout, "Writing '%s'\n", szMsg );
        write ( serial_fd->iFD,
               szMsg,
               strlen(szMsg) );
        /* Sleep 1/10th second */
        usleep ( 100000 );

        /* Update angle */
        angle+=adelta;
        if ( angle >= 360.0f )
        {
        	angle=359.99f;
        	adelta=-adelta;
        }
        else if ( angle <= 0.0f )
        {
        	angle=0.0f;
        	adelta=-adelta;
        }
        	
        speed+=sdelta;
        if ( speed >= 60.0f )
        {
        	speed = 60.0f;
        	sdelta=-sdelta;
        }
        else if ( speed <= 0.0f )
        {
        	speed = 0.0f;
        	sdelta=-sdelta;
        }  
    }

    exit (0);
}

