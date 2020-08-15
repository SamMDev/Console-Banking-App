package com.sammdev.exception;

public class FailedToUpdateRecordException extends RuntimeException {
    public FailedToUpdateRecordException(String message) {
        super(message);
    }
}
