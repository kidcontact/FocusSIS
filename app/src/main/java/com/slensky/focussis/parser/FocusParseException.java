package com.slensky.focussis.parser;

/**
 * Indicates a critical failure while parsing a Focus page
 */
public class FocusParseException extends RuntimeException {
    public FocusParseException() {
    }

    public FocusParseException(String message) {
        super(message);
    }

    public FocusParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FocusParseException(Throwable cause) {
        super(cause);
    }
}
