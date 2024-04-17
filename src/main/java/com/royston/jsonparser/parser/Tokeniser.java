package com.royston.jsonparser.parser;

import java.util.ArrayList;
import java.util.List;

import com.royston.jsonparser.exceptions.InvalidJsonException;
import com.royston.jsonparser.parser.Token.TOKEN;

public class Tokeniser {

    public List<Token> tokens;

    public Tokeniser(String jsonString){
        tokens = new ArrayList<>();
        tokenise(jsonString);
    }

    /**
     * takes a json string and parses it into a {@link List} of tokens.
     * @param jsonString the raw json data to be parsed
     * @throws InvalidJsonException invalid characters appear in the json string
     */
    private void tokenise(String jsonString) throws InvalidJsonException {
        String buffer = "";
        char[] charArray = jsonString.toCharArray();
        for (int i=0; i<charArray.length; i++){
            char c = charArray[i];
            buffer += c;
            switch (c){
                case '{':
                    tokens.add(new Token(TOKEN.CURLY_OPEN, buffer)); break;
                case '}':
                    tokens.add(new Token(TOKEN.CURLY_CLOSE, buffer)); break;
                case '[':
                    tokens.add(new Token(TOKEN.ARRAY_OPEN, buffer)); break;
                case ']':
                    tokens.add(new Token(TOKEN.ARRAY_CLOSE, buffer)); break;
                case ':':
                    tokens.add(new Token(TOKEN.COLON, buffer)); break;
                case ',':
                    tokens.add(new Token(TOKEN.COMMA, buffer)); break;
                case '"':
                    buffer = "";
                    i++; c = charArray[i];
                    while(c != '"' || (c == '"' && charArray[i-1] == '\\')){
                        buffer += c; i++; c = charArray[i];
                    }
                    tokens.add(new Token(TOKEN.STRING, buffer)); break;
                case 'n':
                    char[] nullChars = {'n', 'u', 'l', 'l'};
                    for (int j=1; j<4; j++) if (charArray[i+j] != nullChars[j]) throw new InvalidJsonException("Invalid Json");
                    i+=3; 
                    tokens.add(new Token(TOKEN.NULL_TYPE, "null")); break;
                case 't':
                    char[] trueChars = {'t', 'r', 'u', 'e'};
                    for (int j=1; j<4; j++) if (charArray[i+j] != trueChars[j]) throw new InvalidJsonException("Invalid Json");
                    i+=3;
                    tokens.add(new Token(TOKEN.BOOLEAN, "true")); break;
                case 'f':
                    char[] falseChars = {'f', 'a', 'l', 's', 'e'};
                    for (int j=1; j<5; j++) if (charArray[i+j] != falseChars[j]) throw new InvalidJsonException("Invalid Json");
                    i+=4;
                    tokens.add(new Token(TOKEN.BOOLEAN, "false")); break;
                default:
                    if(c == '-' || (c >= '0' && c <= '9') || c == '.'){
                        i++; c = charArray[i];
                        while(c == '-' || (c >= '0' && c <= '9') || c == '.'){
                            buffer += c; i++; c = charArray[i];
                        }
                        i--;
                        tokens.add(new Token(TOKEN.NUMBER, buffer));
                    }
                    else if (c != ' ' || c != '\n' || c != '\t') buffer = "";
                    else throw new InvalidJsonException("Invalid Json");
            }
            buffer = "";
        }
    }
}
