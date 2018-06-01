package com.jackson42.ebean.genericoptions.exception;

/**
 * BadValueException.
 *
 * @author Pierre Adam
 * @version 18.06.01
 * @since 18.06.01
 */
public class BadValueException extends RuntimeException {
    public BadValueException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
