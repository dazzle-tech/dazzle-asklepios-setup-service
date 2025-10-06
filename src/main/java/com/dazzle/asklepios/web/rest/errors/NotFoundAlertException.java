package com.dazzle.asklepios.web.rest.errors;

import java.net.URI;
import org.springframework.http.HttpStatus;

/**
 * Exception used to send a Not Found (404) response with structured problem details.
 * Extends {@link BadRequestAlertException} but overrides the HTTP status to 404.
 */
@SuppressWarnings("java:S110")
public class NotFoundAlertException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public NotFoundAlertException(String defaultMessage, String entityName, String errorKey) {
        this(ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey);
    }

    public NotFoundAlertException(URI type, String defaultMessage, String entityName, String errorKey) {

        super(type, defaultMessage, entityName, errorKey, HttpStatus.NOT_FOUND);
    }
}
