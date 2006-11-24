#ifndef IF_CONFIG_H_
#define IF_CONFIG_H_

/*************************/
/* Function Prototypes   */
/*************************/
if_status_t IF_load_config ( char *pathname );

if_status_t IF_get_param_as_int    ( char *pname, int *param );
if_status_t IF_get_param_as_float  ( char *pname, float *param );
if_status_t IF_get_param_as_string ( char *pname, char *param, int param_size );

if_status_t IF_get_param_as_int_dflt    ( char *pname, int *param, int dflt );
if_status_t IF_get_param_as_float_dflt  ( char *pname, float *param, float dflt );
if_status_t IF_get_param_as_string_dflt ( char *pname, char *param, int param_size, char *dflt );

#endif /*IF_CONFIG_H_*/
