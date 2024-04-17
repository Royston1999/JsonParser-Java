package com.royston.jsonparser.structures;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.royston.jsonparser.exceptions.JsonTypeException;

public class GenericJsonValue<E> {

    private E value;

    public GenericJsonValue(E object){
        value = object;
    }

    public E getValue() {
        return value;
    }

    public int getInt() throws JsonTypeException {
        if (isNumberType()) return ((Number)value).intValue();
        throw new JsonTypeException("value cannot be cast to an int");
    }

    public OptionalInt getIntOptional() {
        return isNumberType() ? OptionalInt.of(((Number)value).intValue()) : OptionalInt.empty();
    }

    public long getLong() throws JsonTypeException {
        if (isNumberType()) return ((Number)value).longValue();
        throw new JsonTypeException("value cannot be cast to a long");
    }
    
    public OptionalLong getLongOptional() {
        return isNumberType() ? OptionalLong.of(((Number)value).longValue()) : OptionalLong.empty();
    }

    public float getFloat() throws JsonTypeException {
        if (isNumberType()) return ((Number)value).floatValue();
        throw new JsonTypeException("value cannot be cast to a float");
    }
    
    public Optional<Float> getFloatOptional() {
        return isNumberType() ? Optional.of(((Number)value).floatValue()) : Optional.empty();
    }

    public double getDouble() throws JsonTypeException {
        if (isNumberType()) return ((Number)value).doubleValue();
        throw new JsonTypeException("value cannot be cast to a double");
    }
    
    public OptionalDouble getDoubleOptional() {
        return isNumberType() ? OptionalDouble.of(((Number)value).doubleValue()) : OptionalDouble.empty();
    }

    public boolean getBool() throws JsonTypeException {
        if (value instanceof Boolean) return ((Boolean)value).booleanValue();
        throw new JsonTypeException("value cannot be cast to a boolean");
    }

    public Optional<Boolean> getBoolOptional() {
        return value instanceof Boolean ? Optional.of((Boolean)value) : Optional.empty();
    }

    public String getString() throws JsonTypeException {
        if (value instanceof String) return (String)value;
        throw new JsonTypeException("value cannot be cast to a String");
    }

    public Optional<String> getStringOptional() {
        return value instanceof String ? Optional.of((String)value) : Optional.empty();
    }

    public JsonArray getJsonArray() throws JsonTypeException {
        if (value instanceof JsonArray) return (JsonArray)value;
        throw new JsonTypeException("value cannot be cast to a JsonArray");
    }

    public Optional<JsonArray> getJsonArrayOptional() {
        return value instanceof JsonArray ? Optional.of((JsonArray)value) : Optional.empty();
    }

    public JsonObject getJsonObject() throws JsonTypeException {
        if (value instanceof JsonObject) return (JsonObject)value;
        throw new JsonTypeException("value cannot be cast to a JsonObject");
    }

    public Optional<JsonObject> getJsonObjectOptional() {
        return value instanceof JsonObject ? Optional.of((JsonObject)value) : Optional.empty();
    }

    private boolean isNumberType() {
        return value instanceof Number;
    }
}
