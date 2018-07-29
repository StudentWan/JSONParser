package exception;

import java.io.IOException;

public class JsonParseException extends IOException {
    public JsonParseException(String msg) {
        super(msg);
    }
}