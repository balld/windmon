#ifndef IF_DATAACCESS_H_
#define IF_DATAACCESS_H_

if_status_t IF_db_connect ( char *hostname,
                            char *user,
                            char *password,
                            char *dbname );
    	
if_status_t IF_db_disconnect ();


#endif /*IF_DATAACCESS_H_*/
