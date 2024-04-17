package com.royston.jsonparser.structures;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * stores a {@code LinkedHashMap<String, GenericJsonValue<Object>>} of generic objects to mimic the json object structure
 */
public class JsonObject extends JsonStructure {

    private final LinkedHashMap<String, GenericJsonValue<Object>> keyValueMap = new LinkedHashMap<>();

    /**
     * adds object to map and wraps it in a {@link GenericJsonValue}
     * @param key the json string key for the value
     * @param value the value object to add
     */
    public void addValue(String key, Object value){
        keyValueMap.put(key, new GenericJsonValue<>(value));
    }

    /**
     * retrieve a json value from the json object
     * @param key the key to query the map with
     * @return a {@link GenericJsonValue} object containing the json value
     */
    public GenericJsonValue<Object> getValue(String key){
        return keyValueMap.get(key);
    }

    public List<String> keys(){
        return keyValueMap.keySet().stream().collect(Collectors.toList()); 
    }

    @Override
    public JsonObject getJsonObject(){
        return this;
    }

    /**
     * converts the {@link JsonObject} into a raw json string using 4-space indentation
     */
    @Override
    public String toString(){
        return write(1, 4);
    }

    /**
     * converts the {@link JsonObject} into a raw json string
     * @param indentAmount the amount of spaces to add for each indentation block
     * @return the raw json string produced
     */
    public String toString(int indentAmount){
        String output = write(1, indentAmount);
        if (indentAmount == 0) output = output.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "");
        return output;
    }

    /**
     * converts the {@link JsonObject} into a raw json string
     * @param indentLevel the number of indentation blocks deep to start writing from
     * @param indentAmount the amount of spaces to add for each indentation block
     * @return the raw json string produced
     */
    protected String write(int indentLevel, int indentAmount){
        String output = "{";
        List<String> keys = this.keys();
        if (keys.isEmpty()) return "{}";
        for (String key : keys){
            output += newLine(indentLevel, indentAmount);
            Object value = keyValueMap.get(key).getValue();
            if (value == null){
                output += ("\"" + key + "\"" + ": " + "null") + ",";
            }
            else if (value instanceof String){
                output += ("\"" + key + "\"" + ": " + "\"" + value + "\"") + ",";
            }
            else if (value instanceof Boolean){
                output += ("\"" + key + "\"" + ": " + Boolean.toString((Boolean)value)) + ",";
            }
            else if (value instanceof JsonObject){
                output += output += ("\"" + key + "\"" + ": " + ((JsonObject)value).write(indentLevel + 1, indentAmount)) + ",";
            }
            else if (value instanceof JsonArray){
                output += ("\"" + key + "\"" + ": " + ((JsonArray)value).write(indentLevel + 1, indentAmount)) + ",";
            }
            else output += ("\"" + key + "\"" + ": " + value.toString()) + ",";
        }
        output = output.substring(0, output.length()-1);
        output += newLine(indentLevel -1 , indentAmount) + "}";
        return output;
    }

    /**
     * creates a new line for the json string and applies the indentation amount
     * @param indentLevel the number of indentation blocks deep to start writing from
     * @param indentAmount the amount of spaces to add for each indentation block
     * @return the raw json string produced
     */
    private String newLine(int indentLevel, int indentAmount){
        String output = "\n";
        for (int i=0; i<indentLevel; i++){
            for (int j=0; j<indentAmount; j++){
                output += " ";
            }
        }
        return output;
    }
}
