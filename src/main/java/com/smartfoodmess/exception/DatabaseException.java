package com.smartfoodmess.exception;

/**
 * Thrown when something goes wrong while talking to the database
 * (connection failure, broken query, constraint violation, ...).
 *
 * <p>This is an unchecked (runtime) exception so that low level JDBC code
 * does not have to declare it everywhere. The message passed to the user is
 * always friendly; the original SQL error is kept as the <i>cause</i> and is
 * printed to the server log for developers - it is never shown on screen.</p>
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
