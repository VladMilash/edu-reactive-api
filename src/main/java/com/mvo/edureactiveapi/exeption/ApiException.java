package com.mvo.edureactiveapi.exeption;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
