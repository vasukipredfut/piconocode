package com.aria.page.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when an invariant (duplicate code, illegal state transition) is violated. */
@ResponseStatus(HttpStatus.CONFLICT)
public class PageConflictException extends RuntimeException {
    public PageConflictException(String msg) { super(msg); }
}
