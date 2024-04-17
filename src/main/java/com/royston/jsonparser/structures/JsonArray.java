package com.royston.jsonparser.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * stores a {@link List} of generic objects like the json array structure
 */
public class JsonArray extends JsonStructure implements Iterable<GenericJsonValue<Object>> {

    private final List<GenericJsonValue<Object>> valueList = new ArrayList<>();

    /**
     * adds object to array list and wraps it in a {@link GenericJsonValue}
     * @param value object to add
     */
    public void addValue(Object value){
        valueList.add(new GenericJsonValue<>(value));
    }

    /**
     * retrieve a json value from the json array
     * @param index the position in the array
     * @return a {@link GenericJsonValue} object containing the json value
     */
    public GenericJsonValue<Object> getValue(int index){
        return valueList.get(index);
    }

    public int length(){
        return valueList.size();
    }

    @Override
    public JsonArray getJsonArray(){
        return this;
    }

    @Override
    public Iterator<GenericJsonValue<Object>> iterator() {
        return valueList.iterator();
    }

    public Stream<GenericJsonValue<Object>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * converts the {@link JsonArray} into a raw json string using 4-space indentation
     */
    @Override
    public String toString(){
        return write(1, 4);
    }

    /**
     * converts the {@link JsonArray} into a raw json string
     * @param indentAmount the amount of spaces to add for each indentation block
     * @return the raw json string produced
     */
    @Override
    public String toString(int indentAmount){
        String output = write(1, indentAmount);
        if (indentAmount == 0) output = output.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "");
        return output;
    }

    /**
     * converts the {@link JsonArray} into a raw json string
     * @param indentLevel the number of indentation blocks deep to start writing from
     * @param indentAmount the amount of spaces to add for each indentation block
     * @return the raw json string produced
     */
    protected String write(int indentLevel, int indentAmount){
        String output = "[";
        if (valueList.isEmpty()) return "[]";
        for (GenericJsonValue<?> item : valueList){
            Object value = item.getValue();
            output += newLine(indentLevel, indentAmount);
            if (value == null){
                output += ("null,");
            }
            else if (value instanceof String){
                output += ("\"" + value + "\"") + ",";
            }
            else if (value instanceof Boolean){
                output += Boolean.toString((Boolean)value) + ",";
            }
            else if (value instanceof JsonObject){
                output += ((JsonObject)value).write(indentLevel + 1, indentAmount) + ",";
            }
            else if (value instanceof JsonArray){
                output += ((JsonArray)value).write(indentLevel + 1, indentAmount) + ",";
            }
            else output += value.toString() + ",";
        }
        output = output.substring(0, output.length() - 1);
        output += newLine(indentLevel -1 , indentAmount) + "]";
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
