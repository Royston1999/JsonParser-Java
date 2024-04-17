package com.royston.jsonparser.structures;

import com.royston.jsonparser.exceptions.JsonTypeException;

public abstract class JsonStructure {

    public abstract String toString();

    public abstract String toString(int indentAmount);

    public boolean isJsonArray(){
        return this instanceof JsonArray;
    }

    public boolean isJsonObject(){
        return this instanceof JsonObject;
    }

    public JsonArray getJsonArray(){
        throw new JsonTypeException("not a Json Array");
    }

    public JsonObject getJsonObject(){
        throw new JsonTypeException("not a Json Object");
    }
}
