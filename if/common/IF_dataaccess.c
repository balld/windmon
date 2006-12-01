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

#include <mysql/mysql.h>

#include "common/IF_common.h"

/*****************************/
/* Module Variables          */
/*****************************/
static MYSQL mysql;
static if_bool_t connected = IF_FALSE;


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
   mysql_close(&mysql);
}
