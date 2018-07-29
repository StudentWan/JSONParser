package parser;

import exception.JsonParseException;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private Tokenizer tokenizer;

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Json parse() throws Exception {
        Json result = json();
        return result;
    }

    public static JObject parseJSONObject(String s) throws Exception {
        Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader(s)));
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        return parser.object();
    }

    public static JArray parseJSONArray(String s) throws Exception {
        Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader(s)));
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        return parser.array();
    }

    private JObject object() throws IOException {
        tokenizer.next();
        Map<String, Value> map = new HashMap<String, Value>();
        if (isToken(TokenType.END_OBJ)) {
            tokenizer.next();
            return new JObject(map);
        } else if (isToken(TokenType.STRING)) {
            map = key(map);
        }
        return new JObject(map);
    }

    private JArray array() throws IOException  {
        tokenizer.next();
        List<Json> list = new ArrayList<>();
        JArray array = null;

        if (isToken(TokenType.START_ARRAY)) {
            array = array();
            list.add(array);
            if (isToken(TokenType.COMMA)) {
                tokenizer.next();
                list = element(list);
            }
        } else if (isPrimary()) {
            list = element(list);
        } else if (isToken(TokenType.START_OBJ)) {
            list.add(object());
            while (isToken(TokenType.COMMA)) {
                tokenizer.next();
                list.add(object());
            }
        } else if (isToken(TokenType.END_ARRAY)) {
            tokenizer.next();
            array = new JArray(list);
            return array;
        }
        tokenizer.next();
        array = new JArray(list);
        return array;
    }

    private List<Json> element(List<Json> list) throws IOException {
        list.add(new Primary(tokenizer.next().getValue()));
        if (isToken(TokenType.COMMA)) {
            tokenizer.next();
            if (isPrimary()) {
                list = element(list);
            } else if (isToken(TokenType.START_OBJ)) {
                list.add(object());
            } else if (isToken(TokenType.START_ARRAY)) {
                list.add(array());
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        }
        return list;
    }

    private Map<String, Value> key(Map<String, Value> map) throws IOException {
        String key = tokenizer.next().getValue();
        if (!isToken(TokenType.COLON)) {
             throw new JsonParseException("Invalid JSON input.");
        } else {
            tokenizer.next();
            if (isPrimary()) {
                Value primary = new Primary(tokenizer.next().getValue());
                map.put(key, primary);
            } else if (isToken(TokenType.START_ARRAY)) {
                Value array = array();
                map.put(key, array);
            }
            if (isToken(TokenType.COMMA)) {
                tokenizer.next();
                if (isToken(TokenType.STRING)) {
                    map = key(map);
                }
            } else if (isToken(TokenType.END_OBJ)) {
                tokenizer.next();
                return map;
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        }
        return map;
    }

    private Json json() throws IOException {
        TokenType type = tokenizer.peek(0).getType();

        if (type == TokenType.START_ARRAY) {
            return array();
        } else if (type == TokenType.START_OBJ) {
            return object();
        } else {
            throw new JsonParseException("Invalid JSON input");
        }
    }

    private boolean isToken(TokenType tokenType) {
        Token t = tokenizer.peek(0);
        return t.getType() == tokenType;
    }

    private boolean isPrimary() {
        TokenType type = tokenizer.peek(0).getType();
        return type == TokenType.BOOLEAN || type == TokenType.NULL ||
                type == TokenType.NUMBER || type == TokenType.STRING;
    }
}