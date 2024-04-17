package com.royston.jsonparser.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.royston.jsonparser.exceptions.InvalidJsonException;
import com.royston.jsonparser.exceptions.JsonDeserialisationException;
import com.royston.jsonparser.parser.Token.TOKEN;
import com.royston.jsonparser.serialiser.*;
import com.royston.jsonparser.structures.*;

public class JsonParser {

    private static class Pair<K,V> {
        K first;
        V second;
    
        protected Pair(K first, V second){
            this.first = first;
            this.second = second;
        }
    }

    /**
     * parses a json string into a new {@link JsonStructure}
     * @param jsonString the raw json string to parse
     * @return a {@link JsonStructure} containing the parsed data
     * @throws InvalidJsonException the json string is not valid
     */
    public static JsonStructure parseJson(String jsonString) throws InvalidJsonException {
        Tokeniser tokeniser = new Tokeniser(jsonString);
        if (tokeniser.tokens.get(0).type == TOKEN.CURLY_OPEN) return parseObject(tokeniser.tokens, 0).second;
        if (tokeniser.tokens.get(0).type == TOKEN.ARRAY_OPEN) return parseArray(tokeniser.tokens, 0).second;
        throw new InvalidJsonException("json string does not start with an object or array");
    }

    /**
     * parses and deserialises a json string into a user defined class structure.
     * example usage: 
     * <pre> MyJsonObject obj = deserialiseFromJsonString("jsonString", new TypeInfo<>(){}); </pre>
     * @param <T> a user defined class that stores the json data
     * @param jsonString the raw json string to parse and deserialise
     * @param outputType a holder class to pass in the type details for the deserialiser to read 
     * @return an instance of the user defined class structure containing the deserialised json data
     * @throws InvalidJsonException json string is invalid
     * @throws JsonDeserialisationException data cannot be deserialised into the given class structure
     */
    public static <T> T deserialiseFromJsonString(String jsonString, TypeInfo<T> outputType) throws InvalidJsonException, JsonDeserialisationException {
        JsonStructure json = parseJson(jsonString);
        return Serialiser.deserialise(json, outputType);
    }
    
    /**
     * reads in a json string from a file and parses and deserialises it into a user defined class structure
     * example usage: 
     * <pre> MyJsonObject obj = deserialiseJsonFromFile("pathToFile.json", new TypeInfo<>(){}); </pre>
     * @param <T> a user defined class that stores the json data
     * @param fileName the raw json string to parse and deserialise
     * @param outputType a holder class to pass in the type details for the deserialiser to read 
     * @return an instance of the user defined class structure containing the deserialised json data, otherwise null if file cannot be read
     */
    public static <T> T deserialiseJsonFromFile(File file, TypeInfo<T> typeInfo) {
        String json;
        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return deserialiseFromJsonString(json, typeInfo);
    }

    /**
     * parses tokenised json data into a new {@link JsonObject}
     * @param tokens the list of json tokens
     * @param position the current position in the list being parsed
     * @return a {@link Pair} object containing the new position to continue parsing from and a new {@link JsonObject} instance
     * @throws InvalidJsonException there are an invalid sequence of json tokens
     */
    private static Pair<Integer, JsonStructure> parseObject(List<Token> tokens, int position) throws InvalidJsonException {
        JsonObject jsonObject = new JsonObject();
        if (tokens.get(position).type != TOKEN.CURLY_OPEN) throw new InvalidJsonException("Invalid Json");
        position++;
        while (position < tokens.size()){
            if (tokens.get(position).type == TOKEN.STRING){
                String key = tokens.get(position).value; position++;
                if (tokens.get(position).type != TOKEN.COLON) throw new InvalidJsonException("Invalid Json");
                position++;
                String jsonValue = tokens.get(position).value;
                TOKEN type = tokens.get(position).type;
                if (type == TOKEN.STRING) jsonObject.addValue(key, jsonValue);
                else if (type == TOKEN.BOOLEAN) jsonObject.addValue(key, jsonValue == "true" ? true : false);
                else if (type == TOKEN.NULL_TYPE) jsonObject.addValue(key, (null));
                else if (type == TOKEN.NUMBER) jsonObject.addValue(key, parseNumber(jsonValue));
                else if (type == TOKEN.CURLY_OPEN) {
                    Pair<Integer, JsonStructure> parsed = parseObject(tokens, position);
                    position = parsed.first;
                    jsonObject.addValue(key, parsed.second);
                }
                else if (type == TOKEN.ARRAY_OPEN) {
                    Pair<Integer, JsonStructure> parsed = parseArray(tokens, position);
                    position = parsed.first;
                    jsonObject.addValue(key, parsed.second);
                }
            }
            else if (tokens.get(position).type == TOKEN.CURLY_CLOSE) break;
            position++;
            if (tokens.get(position).type == TOKEN.CURLY_CLOSE) break;
            if (tokens.get(position).type != TOKEN.COMMA) throw new InvalidJsonException("Invalid Json");
            position++;
            if (tokens.get(position).type != TOKEN.STRING) throw new InvalidJsonException("Invalid Json");
        }
        return new Pair<>(position, jsonObject);
    }
    
    /**
     * parses tokenised json data into a new {@link JsonArray}
     * @param tokens the list of json tokens
     * @param position the current position in the list being parsed
     * @return a {@link Pair} object containing the new position to continue parsing from and a new {@link JsonArray} instance
     * @throws InvalidJsonException there are an invalid sequence of json tokens
     */
    private static Pair<Integer, JsonStructure> parseArray(List<Token> tokens, int position) throws InvalidJsonException {
        JsonArray jsonArray = new JsonArray();
        if (tokens.get(position).type != TOKEN.ARRAY_OPEN) throw new InvalidJsonException("Invalid Json");
        position++;
        while (position < tokens.size()){
            String jsonValue = tokens.get(position).value;
            TOKEN type = tokens.get(position).type;
            if (type == TOKEN.ARRAY_CLOSE) break;
            if (type == TOKEN.STRING) jsonArray.addValue(jsonValue);
            else if (type == TOKEN.BOOLEAN) jsonArray.addValue(jsonValue == "true" ? true : false);
            else if (type == TOKEN.NULL_TYPE) jsonArray.addValue(null);
            else if (type == TOKEN.NUMBER) jsonArray.addValue(parseNumber(jsonValue));
            else if (type == TOKEN.CURLY_OPEN) {
                Pair<Integer, JsonStructure> parsed = parseObject(tokens, position);
                position = parsed.first;
                jsonArray.addValue(parsed.second);
            }
            else if (type == TOKEN.ARRAY_OPEN) {
                Pair<Integer, JsonStructure> parsed = parseArray(tokens, position);
                position = parsed.first;
                jsonArray.addValue(parsed.second);
            }
            else if (tokens.get(position).type != TOKEN.COMMA) throw new InvalidJsonException("Invalid Json");
            position++;
            if (tokens.get(position).type == TOKEN.ARRAY_CLOSE) break;
            else if (tokens.get(position).type != TOKEN.COMMA) throw new InvalidJsonException("Invalid Json");
            position++;
            if (tokens.get(position).type == TOKEN.COMMA) throw new InvalidJsonException("Invalid Json");
        }
        return new Pair<>(position, (jsonArray));
    }

    /**
     * parses a string into the most appropriate number format type
     * @param numberAsString the string to parse as a number
     * @return the formatted number result as an object
     */
    private static Object parseNumber(String numberAsString) {
        Object outputNumber = null;
        try {
            outputNumber = Integer.valueOf(numberAsString);
        } catch (NumberFormatException e){}
        if (outputNumber != null) return outputNumber;
        try {
            outputNumber = Long.valueOf(numberAsString);
        } catch (NumberFormatException e){}
        if (outputNumber != null) return outputNumber;
        int x = numberAsString.split("\\.")[1].length();
        if (x <= 6) return Float.valueOf(numberAsString);
        return Double.valueOf(numberAsString);
    }
}
