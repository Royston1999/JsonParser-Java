package com.royston.jsonparser.serialiser;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeInfo<T> {
    private final Type type;
    private final Class<?> klass;

    protected TypeInfo(){
        this.type = extractType();
        this.klass = getRawType();
    }

    public Type getType(){
        return this.type;
    }

    public Class<?> getRawClass(){
        return klass;
    }

    private Type extractType() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) superclass;
            if (parameterized.getRawType() == TypeInfo.class) {
                return parameterized.getActualTypeArguments()[0];
            }
        }
        else if (superclass instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) superclass;
            return arrayType.getGenericComponentType();
        }
        return Object.class;
    }

    private Class<?> getRawType() {
        if (type instanceof Class<?>) {
            Class<?> klass = (Class<?>) type;
            return klass.isArray() ? klass.getComponentType() : klass;
        } else {
          ParameterizedType parameterizedType = (ParameterizedType) type;
          Type rawType = parameterizedType.getRawType();
          return (Class<?>) rawType;
        }
    }
}
