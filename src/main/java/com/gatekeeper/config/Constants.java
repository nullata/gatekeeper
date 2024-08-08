package com.gatekeeper.config;

/**
 *
 * @author null
 */
public class Constants {
    
    public static final String APP_NAME = "gatekeeper";
    
    public static final String KEY_HEADER = "x-gate-key";

    //////////////////////////
    // Messages
    //////////////////////////
    public static final String MSG_VALIDATION_START = "Validating environment variables";
    public static final String MSG_VALIDATION_END = "Environment validation complete";
    public static final String MSG_REQEUST_FWD = "Forwarding request: %s";

    //////////////////////////
    // Errors
    //////////////////////////
    public static final String ERR_UNSUPPORTED_VALUE = "Unsupported value for %s: %s";
    public static final String ERR_VALUE_NOT_NUMERIC = "Provided value for %s is not numeric: %s";
    public static final String ERR_ENV_VAR_VAL_FAIL = "Environment variable validation failed: %s";
    public static final String ERR_SERVICE_VAL_FAIL = "Service not started due to runtime validation issues";
    public static final String ERR_RATE_LIMIT_EXCEEDED = "Rate limit exceeded";
    public static final String ERR_REQUEST_FWD_FAIL = "Could not forward request: %s";
    public static final String ERR_SVC_NOT_AVAILABLE = "Service Not Available";
    public static final String ERR_NO_PROXY_TARGET = "No valid proxy target URL available";
    public static final String ERR_PARSE_TARG_FAIL = "Failed to parse %s environment variable";
    public static final String ERR_NO_CLIENT_IP = "Client IP could not be obtained, falling back to round-robin";
    
    //////////////////////////
    // Environment variables
    //////////////////////////
    public static final String ENV_PROXY_TARGET_URL = "PROXY_TARGET_URL";
    
    public static final String ENV_LB_MODE = "LOAD_BALANCE_MODE";
    
    public static final String ENV_DB_TYPE = "DB_TYPE";
    public static final String ENV_DB_HOST = "DB_HOST";
    public static final String ENV_DB_PORT = "DB_PORT";
    public static final String ENV_DB_NAME = "DB_NAME";
    public static final String ENV_DB_USER = "DB_USERNAME";
    public static final String ENV_DB_PASS = "DB_PASSWORD";
    public static final String ENV_DB_TABLE = "TABLE_NAME";
    public static final String ENV_DB_COLUMN = "COLUMN_NAME";

    public static final String ENV_RATE_LIMIT_ENABLED = "RATE_LIMIT_ENABLED";
    public static final String ENV_RATE_LIMIT_RATE = "RATE_LIMIT_RATE";
    public static final String ENV_RATE_LIMIT_TIMEOUT = "RATE_LIMIT_TIMEOUT";
    public static final String ENV_RATE_LIMIT_MODE = "RATE_LIMIT_MODE";
    public static final String ENV_ENABLE_CACHING = "ENABLE_CACHING";
    public static final String ENV_CACHE_MAX_SIZE = "CACHE_MAX_SIZE";
    public static final String ENV_CACHE_MAX_DURATION = "CACHE_MAX_DURATION_M";
    
    //////////////////////////
    // Options
    //////////////////////////
    public static final String OPT_RLM_GLOBAL = "global";
    public static final String OPT_RLM_INDI = "individual";
    
    public static final String OPT_STR_TRUE = "true";
    public static final String OPT_STR_FALSE = "false";
    
    public static final String OPT_LB_RR = "round-robin";
    public static final String OPT_LB_IPH = "ip-hash";
}
