package com.royston.jsonparser.parser;

public class Token{
    public enum TOKEN
    {
        CURLY_OPEN,
        CURLY_CLOSE,
        COLON,
        STRING,
        NUMBER,
        ARRAY_OPEN,
        ARRAY_CLOSE,
        COMMA,
        BOOLEAN,
        NULL_TYPE
    };
    
    public TOKEN type;
    public String value;

    public Token(TOKEN type, String value){
        this.type = type;
        this.value = value;
    }
}
