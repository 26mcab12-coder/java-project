package com.smartfoodmess.exception;

/**
 * Thrown when a login attempt fails because the username does not exist or
 * the password does not match.
 *
 * <p>The message is deliberately generic ("Invalid username or password") so
 * that the application never reveals which part of the login was wrong.</p>
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
