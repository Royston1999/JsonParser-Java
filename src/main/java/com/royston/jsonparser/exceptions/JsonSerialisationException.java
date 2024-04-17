package com.royston.jsonparser.exceptions;

/**
 * An exception that occurs when there is a problem attempting to serialise a class structure into the JSON format
 */
public class JsonSerialisationException extends RuntimeException{
    public JsonSerialisationException(String errorMessage){
        super(errorMessage);
    }
}
