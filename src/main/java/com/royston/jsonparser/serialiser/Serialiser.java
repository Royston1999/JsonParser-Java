package com.royston.jsonparser.serialiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.royston.jsonparser.annotations.*;
import com.royston.jsonparser.exceptions.*;
import com.royston.jsonparser.structures.*;


import java.lang.reflect.*;

public class Serialiser {
    /**
     * serialises a {@link List} object into a {@link JsonArray} class by looping through the list and adding each value
     * @param toSerialise the list to serialise
     * @return the serialised {@link JsonArray} instance
     */
    private static JsonArray serialiseArray(List<?> toSerialise) {
        if (toSerialise == null) return null;
        JsonArray array = new JsonArray();
        if (toSerialise.isEmpty()) return array;
        Object listEntry = toSerialise.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (listEntry == null) toSerialise.forEach(array::addValue);
        else for (Object item : toSerialise) {
            if (item == null) array.addValue(null);
            else {
                Class<?> klass = item.getClass();
                if (item instanceof List) array.addValue(serialiseArray((List<?>)item));
                else if (klass.isArray()) array.addValue(serialiseArray(arrayToList(item, klass.getComponentType())));
                else if (!isJsonPrimitive(klass)) array.addValue(serialiseObject(item));
                else array.addValue(item);
            }
        }
        return array;
    }

    private static JsonObject serialiseObject(Object toSerialise) {
        if (toSerialise == null) return null;
        JsonObject object = new JsonObject();
        try {
            Field[] fields = toSerialise.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(JsonProperty.class)) continue;
                JsonProperty jsonProperty = field.getDeclaredAnnotation(JsonProperty.class);
                String jsonName = jsonProperty.value();
                boolean accessible = field.canAccess(toSerialise);
                if (!accessible) field.setAccessible(true);
                Object value = field.get(toSerialise);
                if (!accessible) field.setAccessible(false);
                if (value == null) { if (!jsonProperty.optional()) object.addValue(jsonName, null); }
                else if (List.class.isAssignableFrom(field.getType())) object.addValue(jsonName, serialiseArray((List<?>)value));
                else if (field.getType().isArray()) object.addValue(jsonName, serialiseArray(arrayToList(value, field.getType().getComponentType())));
                else if (!isJsonPrimitive(field.getType())) object.addValue(jsonName, serialiseObject(value));
                else object.addValue(jsonName, value);
            }
        } catch (IllegalAccessException e) {
            throw new JsonSerialisationException("Inaccessible field with JsonProperty annotation");
        }
        return object;
    }

    /**
     * serialises an object into the {@link JsonObject} class using reflection to loop through the class fields that contain the {@link JsonProperty} annotation
     * @param toSerialise the object to serialise
     * @return the serialised {@link JsonObject} instance
     * @throws JsonSerialisationException unable to access the annotated fields to serialise the data
     */
    public static JsonStructure serialise(Object toSerialise) throws JsonSerialisationException {
        if (toSerialise == null) return null;
        if (toSerialise.getClass().isArray()) return serialiseArray(arrayToList(toSerialise, toSerialise.getClass().getComponentType()));
        if (toSerialise instanceof List) return serialiseArray((List<?>)toSerialise);
        return serialiseObject(toSerialise);
    }

    /**
     * convert an array of objects into a list. handles converting an array of primitives into a list
     * @param array an object representing the array to convert
     * @param klass the specific class type the list contains
     * @return the list containing the values
     */
    private static List<?> arrayToList(Object array, Class<?> klass){
        if (klass.isAssignableFrom(int.class)){
            return Arrays.stream((int[])array).boxed().collect(Collectors.toList());
        }
        if (klass.isAssignableFrom(long.class)){
            return Arrays.stream((long[])array).boxed().collect(Collectors.toList());
        }
        if (klass.isAssignableFrom(float.class)){
            List<Float> floatList = new ArrayList<>();
            for (float value : (float[])array) floatList.add(Float.valueOf(value));
            return floatList;
        }
        if (klass.isAssignableFrom(double.class)){
            return Arrays.stream((double[])array).boxed().collect(Collectors.toList());
        }
        return Arrays.asList((Object[])array);
    }

    /**
     * deserialises a json array into a {@link List} of objects
     * @param toDeserialise the json array to deserialise
     * @param listType the parameterised type of the array
     * @return a {@link List} of objects of the result type
     */
    private static Object deserialiseArray(JsonArray toDeserialise, Type listType) {
        if (toDeserialise == null) return null;
        List<Object> list = new ArrayList<>();
        boolean isParameterized = listType instanceof ParameterizedType;
        Type type = isParameterized ? ((ParameterizedType)listType).getActualTypeArguments()[0] : ((Class<?>)listType).getComponentType();
        for (GenericJsonValue<?> item : toDeserialise) {
            if (type instanceof ParameterizedType && List.class.isAssignableFrom((Class<?>)((ParameterizedType)type).getRawType())) {
                list.add(deserialiseArray((JsonArray)item.getValue(), (ParameterizedType)type));
            }
            else if (((Class<?>) type).isArray()) list.add(deserialiseArray((JsonArray)item.getValue(), type));
            else if (!isJsonPrimitive((Class<?>) type)) list.add(deserialiseObject((JsonObject)item.getValue(), (Class<?>) type));
            else list.add(item.getValue());
        }
        return isParameterized ? list : listToArray(list, (Class<?>)type);
    }

    /**
     * convert a list of of objects into an array. handles converting numbers into an array of primitives
     * @param list the list of objects to convert
     * @param klass the specific class type the list contains
     * @return an object representing the array
     */
    private static Object listToArray(List<Object> list, Class<?> klass){
        if (klass.isAssignableFrom(int.class)){
            return list.stream().map(v -> (Integer)v).mapToInt(Integer::intValue).toArray();
        }
        if (klass.isAssignableFrom(long.class)){
            return list.stream().map(v -> (Long)v).mapToLong(Long::longValue).toArray();
        }
        if (klass.isAssignableFrom(float.class)){
            float[] floatArray = new float[list.size()];
            int index = 0;
            for (Object item : list) floatArray[index++] = item instanceof Float ? ((Float)item).floatValue() : ((Double)item).floatValue();
            return floatArray;
        }
        if (klass.isAssignableFrom(double.class)){
            return list.stream().map(v -> v instanceof Float ? ((Float)v).doubleValue() : (Double)v).mapToDouble(Double::doubleValue).toArray();
        }
        return list.toArray((Object[])Array.newInstance(klass, 0));
    }

    /**
     * deserialise an {@link JsonStructure} value. example usage: 
     * <pre> MyClass obj = deserialise(myJsonArray, new TypeInfo<>(){}); </pre>
     * @param <T> a user defined class that stores the json data
     * @param json the JsonStructure to deserialise
     * @param info the type info of the output type
     * @return an instance of the user defined class structure containing the deserialised json data
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialise(JsonStructure json, TypeInfo<T> info){
        if (json == null) return null;
        if (json instanceof JsonArray) return (T)deserialiseArray((JsonArray)json, info.getType());
        if (json instanceof JsonObject) return (T)deserialiseObject((JsonObject)json, info.getRawClass());
        return null;
    }

    /**
     * deserialises a json object into a new object instance
     * @param object the json to deserialise
     * @param resultType the result object type
     * @return the new object instance
     * @throws JsonDeserialisationException unable to access the fields to populate the new object instance
     */
    private static Object deserialiseObject(JsonObject object, Class<?> resultType) {
        if (object == null) return null;
        Constructor<?> constructor = getConstructor(resultType);
        return constructor.getParameterCount() > 0 ? instantiateWithParameterisedConstructor(object, constructor) : instantiateWithDefaultConstructor(object, constructor);
    }

    private static Object instantiateWithDefaultConstructor(JsonObject object, Constructor<?> constructor) throws JsonDeserialisationException {
        Object newObject = null;
        try {
            newObject = constructor.newInstance();
            Field[] fields = newObject.getClass().getDeclaredFields();
            for (String currentKey : object.keys()) {
                Field field = Arrays.stream(fields).filter(f -> {
                    if (!f.isAnnotationPresent(JsonProperty.class)) return false;
                    String serialisedName = f.getDeclaredAnnotation(JsonProperty.class).value();
                    return serialisedName.equals(currentKey);
                }).findFirst().orElse(null);
                if (field == null) continue;
                Class<?> fType = field.getType();

                boolean accessible = field.canAccess(newObject);
                if (!accessible) field.setAccessible(true);                
                if (List.class.isAssignableFrom(field.getType())) {
                    field.set(newObject, deserialiseArray((JsonArray)object.getValue(currentKey).getValue(), field.getGenericType()));
                }
                else if (fType.isArray()) field.set(newObject, deserialiseArray((JsonArray)object.getValue(currentKey).getValue(), fType));
                else if (!isJsonPrimitive(fType)) field.set(newObject, deserialiseObject((JsonObject)object.getValue(currentKey).getValue(), fType));
                else field.set(newObject, object.getValue(currentKey).getValue());
                if (!accessible) field.setAccessible(false);
            }
        }
        catch (InstantiationException | IllegalAccessException  | IllegalArgumentException | InvocationTargetException e) {
            throw new JsonDeserialisationException("cannot create/populate fields of the object");
        }
        return newObject;
    }

    /**
     * creates a new instance of an object using a given constructor
     * @param obj the json to deserialoise
     * @param constructor the constructor to use to create the object
     * @return the new object instance
     * @throws JsonDeserialisationException unable to adequately provide parameters for the constructor or the object was unable to be instantiated
     */
    private static Object instantiateWithParameterisedConstructor(JsonObject obj, Constructor<?> constructor) throws JsonDeserialisationException {
        JsonConstructor constructorAnnotation = constructor.getAnnotation(JsonConstructor.class);
        Parameter[] params = constructor.getParameters();
        List<JsonParam> paramNames = Arrays.stream(params)
                                    .map(p -> Arrays.stream(p.getAnnotations())
                                    .filter(a -> a.annotationType().isAssignableFrom(JsonParam.class))
                                    .findFirst()
                                    .map(JsonParam.class::cast)
                                    .orElse(null))
                                    .collect(Collectors.toList());
        Object[] parameterPack = new Object[constructor.getParameterCount()];
        for (int i=0; i<parameterPack.length; i++){
            if (paramNames.get(i) == null && !constructorAnnotation.allowNonAnnotatedParams()) throw new JsonDeserialisationException("no annotation???");
            GenericJsonValue<Object> jsonValue = obj.getValue(paramNames.get(i).value());
            if (jsonValue == null && !paramNames.get(i).nullable()) throw new JsonDeserialisationException("Non-Nullable parameter does not exist in Json");
            Object value = jsonValue != null ? jsonValue.getValue() : null;

            Class<?> type = params[i].getType();
            if (List.class.isAssignableFrom(type)) parameterPack[i] = deserialiseArray((JsonArray)value, params[i].getParameterizedType());   
            else if (type.isArray()) parameterPack[i] =  deserialiseArray((JsonArray)value, type);
            else if (!isJsonPrimitive(type)) parameterPack[i] = deserialiseObject((JsonObject)value, type);
            else parameterPack[i] = value;
        }
        Object newObj = null;
        try{
            newObj = constructor.newInstance(parameterPack);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new JsonDeserialisationException("could not instantiate object");
        }
        return newObj;
    }

    /**
     * searches for a valid constructor to use for deserialisation
     * @param klass the class to search
     * @return the constructor to use
     * @throws JsonDeserialisationException no valid constructor exists
     */
    private static Constructor<?> getConstructor(Class<?> klass) throws JsonDeserialisationException {
        Constructor<?> usableConstructor = null;
        Constructor<?>[] constructors = klass.getConstructors();
        List<Constructor<?>> annotatedConstructors = Arrays.stream(constructors).filter(c -> c.isAnnotationPresent(JsonConstructor.class)).collect(Collectors.toList());
        if (annotatedConstructors.size() > 1) throw new JsonDeserialisationException("Cannot have more than 1 Json Contructor");
        else if (annotatedConstructors.size() == 1) usableConstructor = annotatedConstructors.get(0);
        else usableConstructor = Arrays.stream(constructors).filter(c -> c.getParameterCount() == 0).findFirst().orElse(null);
        if (usableConstructor == null) throw new JsonDeserialisationException("No usable constructors for deserialisation");
        return usableConstructor;
    }

    /**
     * checks if the given class is a json primitive type
     * @param type the type to check
     * @return true if it is a json primitive
     */
    private static boolean isJsonPrimitive(Class<?> type) {
        return type.isAssignableFrom(String.class) || type.isAssignableFrom(boolean.class) || type.isAssignableFrom(float.class) 
                || type.isAssignableFrom(int.class) || type.isAssignableFrom(long.class) || type.isAssignableFrom(double.class)
                || type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(Float.class) || type.isAssignableFrom(Integer.class) 
                || type.isAssignableFrom(Long.class) || type.isAssignableFrom(Double.class);
    }
}
