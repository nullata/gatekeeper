package com.gatekeeper.exceptions;

/**
 *
 * @author null
 */
public class UnsupportedbTypeException extends RuntimeException {

    public UnsupportedbTypeException(String dbType) {
        super("Unsupported DB_TYPE: " + dbType);
    }
    
}
