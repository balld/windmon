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

#include "IF_common.h"
#include "IF_config.h"
#include "hashtable.h"

/******************************/
/* internal definitions       */
/******************************/
#define CONFIG_BUFFER_LEN 1024

typedef struct _config_elem
{
	char pname[CONFIG_BUFFER_LEN];
	char param[CONFIG_BUFFER_LEN];
} config_elem_t;

static struct hashtable  *htbl = NULL;

/******************************/
/* static function prototypes */
/******************************/
static if_status_t read_config ( FILE *fp );
static if_status_t save_param ( char *pname, char *param );

static unsigned int hash_fn( void *k );
static int          key_cmp_fn ( void *key1, void *key2 );


/******************************/
/* static functions           */
/******************************/
static if_status_t read_config ( FILE *fp )
{
	char buffer[CONFIG_BUFFER_LEN];
	char pname[CONFIG_BUFFER_LEN];
	char param[CONFIG_BUFFER_LEN];
	int  pname_len = 0;
	int  param_len = 0;
	int  line_no = 0;
	int  i = 0;
	

	char *tmp_ptr = NULL;
	
	while ( fgets ( buffer, CONFIG_BUFFER_LEN, fp ) != NULL )
	{
		line_no++;
		
		/* Check line is terminated with newline */
		if ( buffer[strlen(buffer)-1] != '\n'
		     && buffer[strlen(buffer)-1] != EOF )
		{
			IF_log_event ( 0,
			               IF_SEV_ERROR,
			               "Line '%d' too long or not terminated correctly.",
			               line_no );
			return ( IF_ERR );
		}
		
		/* Remove trailing newline/EOF */
		buffer[strlen(buffer)-1] = '\0';
		
		/* Remove all characters after '#' - these are comments */
		if ( ( tmp_ptr = strchr ( buffer, (int) '#' ) ) != NULL )
		{
			*tmp_ptr = '\0';
		}
		
		/* Find first occurrence of '='. This divides param name from value */
		if ( ( tmp_ptr = strchr ( buffer, (int) '=' ) ) != NULL )
		{
			pname_len = tmp_ptr - buffer;
			i = 0;
			/* Skip leading white space */
			while ( i < pname_len && isspace(buffer[i]) )
			{
				i++;
			}
			/* Copy param name */
			strncpy ( pname, &(buffer[i]), pname_len - i );
			pname[pname_len - i] = '\0';
			
			/* Delete trailing spaces */
			i = strlen(pname) - 1;
			while ( i >= 0 && isspace(pname[i]) )
			{
				pname[i] = '\0';
				i--;
			}

			/* Zero length parameter name - no good ! */
			if ( i < 0 )
			{
				IF_log_event ( 0,
				               IF_SEV_WARN,
				               "Line '%d' corrupt. No param name in '%s'",
				               line_no,
				               buffer );
				/* Skip to next don't save */
				continue;
			}
			
			/* Increment tmp_ptr past '=' */
			tmp_ptr++;
			
			/* calculate length */ 
			param_len = strlen ( tmp_ptr );
			
			/* Skip leading white space */
			i = 0;
			while ( i < param_len && isspace(tmp_ptr[i]) )
			{
				i++;
			}
			
			/* Copy parameter value */
			strncpy ( param, &(tmp_ptr[i]), param_len - i );
			param[param_len - i] = '\0';

			/* Delete trailing spaces */
			i = strlen(param) - 1;
			while ( i >= 0 && isspace(param[i]) )
			{
				param[i] = '\0';
				i--;
			}

			/* Zero length parameter - no good ! */
			if ( i < 0 )
			{
				IF_log_event ( 0,
				               IF_SEV_WARN,
				               "Line '%d' corrupt. No param value in '%s'",
				               line_no,
				               buffer );
				/* Skip to next don't save */
				continue;
			}
			
			IF_log_event ( 0,
		                   IF_SEV_INFO,
		                   "Saving config parameter '%s'='%s'",
		                   pname, param );
			save_param ( pname, param );
		}
		else if ( strlen(buffer) == 0 )
		{
			/* Silently ignore */
		}
		else
		{
			IF_log_event ( 0,
			               IF_SEV_WARN,
			               "Line '%d' corrupt. No '=' in '%s'",
			               line_no,
			               buffer );
		}
	} /* while fgets */
	
	return (IF_OK);
}

static if_status_t save_param ( char *pname, char *param )
{
	char *found = NULL;
	char *p, *n;
	
	/* Check for existing value and remove */
    if ( (found = hashtable_search(htbl,pname)) != NULL )
    {
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Overwriting previous config parameter value '%s'='%s'",
		               pname, param );
      	hashtable_remove(htbl,pname);
    }

	/* Insert paramater value */
	p = calloc ( CONFIG_BUFFER_LEN, sizeof(char) );
	n = calloc ( CONFIG_BUFFER_LEN, sizeof(char) );
	strcpy ( p, param );
	strcpy ( n, pname );

    if ( !hashtable_insert(htbl,n,p) )
    {
		IF_log_event ( 0,
		               IF_SEV_FATAL,
		               "Could not save config parameter '%s'='%s'",
		               pname, param );
    	exit(1);
	}
	return IF_OK;	
}


/******************************/
/* public functions           */
/******************************/

if_status_t IF_load_config ( char *pathname )
{
	if_status_t retval = IF_OK;
	
	if ( htbl != NULL )
	{
		hashtable_destroy(htbl,1);
	}
	htbl = create_hashtable(16, hash_fn, key_cmp_fn);
		
	FILE *fp = NULL;
	
	if ( ( fp = fopen ( pathname, "r" ) ) == NULL )
	{
		IF_log_event_errno ( 0,
		                     IF_SEV_ERROR,
		                     "Could not open config file '%s'", pathname );
	}
	else
	{
		retval = read_config ( fp );
	}

	if ( fp != NULL )
	{
		fclose ( fp );
	}	
	return retval;
}
	

if_status_t IF_get_param_as_int    ( char *pname, int *param )
{
	char *pval = NULL;
	
	if ( ( pval = hashtable_search(htbl,pname) ) == NULL )
	{
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Could not find config paramater '%s'",
		               pname);
		return IF_ERR;
	}
	else
	{
		*param = atoi ( pval );
		return IF_OK;
	}
}
if_status_t IF_get_param_as_float  ( char *pname, float *param )
{
	char *pval = NULL;
	
	if ( ( pval = hashtable_search(htbl,pname) ) == NULL )
	{
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Could not find config paramater '%s'",
		               pname);
		return IF_ERR;
	}
	else
	{
		*param = atof ( pval );
		return IF_OK;
	}
}

if_status_t IF_get_param_as_string ( char *pname, char *param, int param_size )
{
	char *pval = NULL;
	
	if ( ( pval = hashtable_search(htbl,pname) ) == NULL )
	{
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Could not find config paramater '%s'",
		               pname);
		return IF_ERR;
	}
	else
	{
		strncpy ( param, pval, param_size );
		param[param_size-1] = '\0';
		return IF_OK;
	}
}
	

if_status_t IF_get_param_as_int_dflt    ( char *pname, int *param, int dflt )
{
	/* Try to fetch parameter. If not successful return default. */
	if ( IF_get_param_as_int( pname, param ) != IF_OK )
	{
		*param = dflt;
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Using default '%s' = '%d'",
		               pname, *param);
	}
	
	/* Always successful */
	return IF_OK;
}

if_status_t IF_get_param_as_float_dflt  ( char *pname, float *param, float dflt )
{
	/* Try to fetch parameter. If not successful return default. */
	if ( IF_get_param_as_float( pname, param ) != IF_OK )
	{
		*param = dflt;
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Using default '%s' = '%f'",
		               pname, *param);
	}
	
	/* Always successful */
	return IF_OK;
}

if_status_t IF_get_param_as_string_dflt ( char *pname, char *param, int param_size, char *dflt )
{
	/* Try to fetch parameter. If not successful return default. */
	if ( IF_get_param_as_string( pname, param, param_size ) != IF_OK )
	{
		strncpy ( param, dflt, param_size );
		param[param_size - 1] = '\0';
		IF_log_event ( 0,
		               IF_SEV_WARN,
		               "Using default '%s' = '%s'",
		               pname, param);
	}
	
	/* Always successful */
	return IF_OK;
}




static unsigned int hash_fn( void *k )
{
   unsigned long hash = 0;
   int c;
   char *str = (char *) k;

   while ((c = *str++) != '\0')
   {
       hash = c + (hash << 6) + (hash << 16) - hash;
   }
   return hash;
}

static int key_cmp_fn ( void *key1, void *key2 )
{
	if ( strcmp( (char *) key1, (char *) key2) == 0 )
	{
		return ( 1 );
	}
	else
	{
		return ( 0 );
	}
}
