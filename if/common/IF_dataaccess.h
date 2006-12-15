#ifndef IF_DATAACCESS_H_
#define IF_DATAACCESS_H_

if_status_t IF_db_connect ( char *hostname,
                            char *user,
                            char *password,
                            char *dbname );
    	
if_status_t IF_db_disconnect ();

if_status_t IF_db_log_wind_record ( time_t iStartDTM,
									time_t iEndDTM,
									int iNumMeasurements,
									float fSpeedMin,
									float fSpeedMax,
									float fSpeedAve,
									float fAngleAve);

#endif /*IF_DATAACCESS_H_*/
