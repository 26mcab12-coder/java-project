package com.smartfoodmess.exception;

/**
 * Thrown when user input is missing, blank, wrongly formatted, or breaks a
 * simple business rule (for example a duplicate username, a negative price
 * or a quantity of zero).
 *
 * <p>The message stored in this exception is written for end users, so it is
 * safe to display it directly on the web page. Technical/SQL details are
 * never placed here.</p>
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
