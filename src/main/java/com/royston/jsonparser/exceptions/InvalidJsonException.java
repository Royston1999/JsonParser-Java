package com.royston.jsonparser.exceptions;

/**
 * An exception that occurs when invalid JSON is being parsed
 */
public class InvalidJsonException extends RuntimeException{
    public InvalidJsonException(String errorMessage){
        super(errorMessage);
    }
}
