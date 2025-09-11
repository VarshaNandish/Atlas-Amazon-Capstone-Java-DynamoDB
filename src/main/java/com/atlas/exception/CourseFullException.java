package com.atlas.exception;

public class CourseFullException extends RuntimeException {
    public CourseFullException(String message) {
        super(message);
    }
}
