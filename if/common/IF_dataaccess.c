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

#include <mysql/mysql.h>

#include "common/IF_common.h"

/*****************************/
/* Module Variables          */
/*****************************/
static MYSQL mysql;

static MYSQL_STMT *stmtInsertRecord = NULL;

static if_bool_t connected = IF_FALSE;

/*****************************/
/* SQL Statments            */
/*****************************/
#define WIND_REC_INSERT_STMT "INSERT INTO wind_data_record " \
                             "(start_dtm, end_dtm, reading_count, min_speed, max_speed, ave_speed, ave_angle) " \
                             "VALUES(?,?,?,?,?,?,?)"

/******************************/
/* local function definitions */
/******************************/
void convert_time ( time_t t, MYSQL_TIME *m );
	

/*****************************/
/* Connect To Database       */
/*****************************/
if_status_t IF_db_connect ( char *hostname,
                            char *user,
                            char *password,
                            char *dbname )
{
	if_status_t retval = IF_OK;
	
	mysql_init(&mysql);

   // mysql_options(&mysql,MYSQL_OPT_COMPRESS,0);/*call only if required otherwise omit*/

   // mysql_options(&mysql,MYSQL_READ_DEFAULT_GROUP,"jahans_Dhaka_Stock_Exchange_Game");/*call only if required otherwise omit*/

   if ( mysql_real_connect(&mysql,
                           hostname,
                           user,
                           password,
                           dbname,
                           0,             /* port */
                           NULL,          /* unix socket */
                           0) != NULL )   /* client_flag */
    {
		IF_log_event ( 0,
	                   IF_SEV_INFO,
	                   "Connected to database '%s' @ '%s' as '%s'",
	                   dbname, hostname, user );
	    connected = IF_TRUE;
    }
    else
    {
		IF_log_event ( 0,
	                   IF_SEV_ERROR,
	                   "Failed to connect to database '%s' @ '%s' as '%s'. "
	                   "'%s' [%d]",
	                   dbname, hostname, user,
	                   mysql_error(&mysql), mysql_errno(&mysql) );
	    connected = IF_FALSE;
	    retval = IF_ERR;
    }
    
    return (retval);
}	


if_status_t IF_db_disconnect ()
{
	if ( stmtInsertRecord != NULL )
	{
		mysql_stmt_close(stmtInsertRecord);
		stmtInsertRecord = NULL;
	}
	mysql_close(&mysql);
	return ( IF_OK );
}

if_status_t IF_db_log_wind_record ( time_t iStartDTM,
									time_t iEndDTM,
									int iNumMeasurements,
									float fSpeedMin,
									float fSpeedMax,
									float fSpeedAve,
									float fAngleAve)
{
	/* static function variables */
	static MYSQL_BIND    bind[7];
	static MYSQL_TIME bStartDTM;
	static MYSQL_TIME bEndDTM;
	static int ibNumMeasurements;
	static float fbSpeedMin;
	static float fbSpeedMax;
	static float fbSpeedAve;
	static float fbAngleAve;

	/* function variables */
	if_status_t retval = IF_OK;

	/* Do one-off setup if insert statement has not been prepared */
	if ( stmtInsertRecord == NULL )
	{
		stmtInsertRecord = mysql_stmt_init(&mysql);
		
		if (mysql_stmt_prepare(stmtInsertRecord, WIND_REC_INSERT_STMT, strlen(WIND_REC_INSERT_STMT)))
		{
			IF_log_event ( 0,
		                   IF_SEV_ERROR,
		                   "Failed to prepare statement for wind data record insert: '%s' [%d]",
		                   mysql_stmt_error(stmtInsertRecord), mysql_stmt_errno(stmtInsertRecord) );
			mysql_stmt_close(stmtInsertRecord);
			stmtInsertRecord = NULL;
	        retval = IF_ERR;
		}
		memset ( bind, 0, sizeof(bind) );
		
		/* start datetime */
		bind[0].buffer_type = MYSQL_TYPE_DATETIME; 
		bind[0].buffer = &bStartDTM;
		bind[0].buffer_length = sizeof(bStartDTM);
		bind[0].length = 0; /* ignored for numeric & temporal types */
		bind[0].is_null = (my_bool*) 0;    /* Never null */
		bind[0].is_unsigned = (my_bool) 0; /* signed */

		/* end datetime */
		bind[1].buffer_type = MYSQL_TYPE_DATETIME; 
		bind[1].buffer = &bEndDTM;
		bind[1].buffer_length = sizeof(bEndDTM);
		bind[1].length = 0; /* ignored for numeric & temporal types */
		bind[1].is_null = (my_bool*) 0;    /* Never null */
		bind[1].is_unsigned = (my_bool) 0; /* signed */

		/* number of measurements */
		bind[2].buffer_type = MYSQL_TYPE_LONG; 
		bind[2].buffer = &ibNumMeasurements;
		bind[2].buffer_length = sizeof(ibNumMeasurements);
		bind[2].length = 0; /* ignored for numeric & temporal types */
		bind[2].is_null = (my_bool*) 0;    /* Never null */
		bind[2].is_unsigned = (my_bool) 0; /* signed */

		/* minimum speed */
		bind[3].buffer_type = MYSQL_TYPE_FLOAT; 
		bind[3].buffer = &fbSpeedMin;
		bind[3].buffer_length = sizeof(fbSpeedMin);
		bind[3].length = 0; /* ignored for numeric & temporal types */
		bind[3].is_null = (my_bool*) 0;    /* Never null */
		bind[3].is_unsigned = (my_bool) 0; /* signed */

		/* maximum speed */
		bind[4].buffer_type = MYSQL_TYPE_FLOAT; 
		bind[4].buffer = &fbSpeedMax;
		bind[4].buffer_length = sizeof(fbSpeedMax);
		bind[4].length = 0; /* ignored for numeric & temporal types */
		bind[4].is_null = (my_bool*) 0;    /* Never null */
		bind[4].is_unsigned = (my_bool) 0; /* signed */

		/* average speed */
		bind[5].buffer_type = MYSQL_TYPE_FLOAT; 
		bind[5].buffer = &fbSpeedAve;
		bind[5].buffer_length = sizeof(fbSpeedAve);
		bind[5].length = 0; /* ignored for numeric & temporal types */
		bind[5].is_null = (my_bool*) 0;    /* Never null */
		bind[5].is_unsigned = (my_bool) 0; /* signed */
		 
		/* average angle */
		bind[6].buffer_type = MYSQL_TYPE_FLOAT; 
		bind[6].buffer = &fbAngleAve;
		bind[6].buffer_length = sizeof(fbAngleAve);
		bind[6].length = 0; /* ignored for numeric & temporal types */
		bind[6].is_null = (my_bool*) 0;    /* Never null */
		bind[6].is_unsigned = (my_bool) 0; /* signed */
		
		if ( mysql_stmt_bind_param ( stmtInsertRecord, bind ) != 0 )
		{
			IF_log_event ( 0,
		                   IF_SEV_ERROR,
		                   "Failed to bind wind data record input parameters: '%s' [%d]",
		                   mysql_stmt_error(stmtInsertRecord), mysql_stmt_errno(stmtInsertRecord) );
			mysql_stmt_close(stmtInsertRecord);
			stmtInsertRecord = NULL;
	        retval = IF_ERR;
		}
	}		

	convert_time ( iStartDTM, &bStartDTM );
	convert_time ( iEndDTM,   &bEndDTM   );
	ibNumMeasurements = iNumMeasurements; 
	fbSpeedMin = fSpeedMin;
	fbSpeedMax = fSpeedMax;
	fbSpeedAve = fSpeedAve;
	fbAngleAve = fAngleAve;	
	
	/* Execute the INSERT statement */
	if (mysql_stmt_execute(stmtInsertRecord) != 0 )
	{
		IF_log_event ( 0,
	                   IF_SEV_ERROR,
	                   "Failed to insert wind data record : '%s' [%d]",
	                   mysql_error(&mysql), mysql_errno(&mysql) );
	    retval = IF_ERR;
	}
		
	return retval;
}


void convert_time ( time_t t, MYSQL_TIME *m )
{
	struct tm *ptm = NULL;
	
	if ( m == NULL )
	{
		return;
	}

	ptm = localtime ( &t );
	
	m->year  = ptm->tm_year + 1900; /* tm_year is years since 1900 */
	m->month = ptm->tm_mon + 1;     /* tm_mon is 0 .. 11 */
	m->day   = ptm->tm_mday;        /* tm_mday is 0.. 31 */
	m->hour  = ptm->tm_hour;
	m->minute= ptm->tm_min;
	m->second= ptm->tm_sec;
	m->neg = 0;
	m->second_part = 0; /* Not used by MYSQL yet anyway */
}

