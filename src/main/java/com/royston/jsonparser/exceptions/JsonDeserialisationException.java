package com.royston.jsonparser.exceptions;

/**
 * An exception that occurs when there is a problem attempting to deserialise the JSON into a user defined class structure
 */
public class JsonDeserialisationException extends RuntimeException{
    public JsonDeserialisationException(String errorMessage){
        super(errorMessage);
    }
}
