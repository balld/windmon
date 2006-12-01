#ifndef IF_LOG_H_
#define IF_LOG_H_

/***********************/
/* General definitions */
/***********************/

/* Status type - generally for function return values */
typedef int  if_status_t;
#define IF_ERR   0
#define IF_OK    1
#define IF_EOF   1

/* Simple boolean type */
typedef int  if_bool_t;
#define IF_FALSE 0
#define IF_TRUE  1

/* Log event severity */
typedef char if_event_sev_t;
#define IF_SEV_INFO  'I'
#define IF_SEV_WARN  'W'
#define IF_SEV_ERROR 'E'
#define IF_SEV_FATAL 'F'

/* Max number of waiting connections queued on incoming socket */
#define IF_SOCKET_BACKLOG 5

/* Buffer size for reading socket data */
#define IF_BUFFER_SIZE  255

/* NMEA structure sizes */
#define IF_NMEA_RECORD_SIZE  1024
#define IF_MAX_NMEA_FIELDS     10
#define IF_NMEA_FIELD_SIZE    256


/***********************/
/* Data Structures     */
/***********************/

/* Full info on a file descriptor including input buffers */
typedef struct _TFDInfo
{
    if_bool_t bDynamic;
    int iFD;
    int iInBufferIndex;
    char zInBuffer[IF_BUFFER_SIZE + 1];
    char zReadyData[IF_BUFFER_SIZE + 1];
    char zDesc[1024]; 
} if_fd_info_t;

typedef struct
{
    char szRawData[IF_NMEA_RECORD_SIZE];
    char aszFields[IF_MAX_NMEA_FIELDS][IF_NMEA_FIELD_SIZE];
    if_bool_t bProprietary;
    char szTalkerIDString[IF_NMEA_FIELD_SIZE];
    char szSentenceIDString[IF_NMEA_FIELD_SIZE];
    char szManufacturerIDString[IF_NMEA_FIELD_SIZE];
    if_bool_t bValid;
    int iNumFields;
} if_nmea_msg_t;


/***********************/
/* Function Prototypes */
/***********************/

/*
 * Event Logging
 */
void _IF_log_event ( int errnoFlag,
                     int errnum, if_event_sev_t sev,
                     char *fmt, va_list arg_list );
void IF_log_event ( int errnum, if_event_sev_t sev, char *fmt, ... );
void IF_log_event_errno ( int errnum, if_event_sev_t sev, char *fmt, ... );

/*
 * Files and sockets
 */
if_fd_info_t *IF_fd_init (if_fd_info_t *psFDInfo);

if_bool_t IF_fd_is_open (if_fd_info_t *psFDInfo);

if_status_t IF_fd_read (if_fd_info_t *psFDInfo);

if_status_t IF_fd_dispose (if_fd_info_t *psFDInfo);

if_fd_info_t *IF_serial_open( char *zDeviceName,
                            long lBaudRate,
                            int iDataBits,
                            int iStopBits, int iParity);

if_fd_info_t *IF_socket_open (unsigned short sPortNum);

if_fd_info_t *IF_socket_listen ();

if_status_t IF_parse_nmea_msg ( if_nmea_msg_t *pMsg, char *szStr );

void IF_log_nmea_msg ( if_nmea_msg_t *pMsg );



#endif /*IF_LOG_H_*/
