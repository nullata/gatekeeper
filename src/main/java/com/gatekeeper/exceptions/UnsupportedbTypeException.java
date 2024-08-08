package com.gatekeeper.exceptions;

import com.gatekeeper.config.Constants;

/**
 *
 * @author null
 */
public class UnsupportedbTypeException extends RuntimeException {

    public UnsupportedbTypeException(String dbType) {
        super(String.format(Constants.ERR_UNSUPPORTED_VALUE, Constants.ENV_DB_TYPE, dbType));
    }
    
}
