package com.royston.jsonparser.exceptions;

/**
 * An exception that occurs when the user attempts to retrieve a json value of the incorrect type
 */
public class JsonTypeException extends RuntimeException{
    public JsonTypeException(String errorMessage){
        super(errorMessage);
    }
}
