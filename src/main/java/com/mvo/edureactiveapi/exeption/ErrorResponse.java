package com.mvo.edureactiveapi.exeption;

public record ErrorResponse(String timestamp, int status, String error, String message, String path) {
}
