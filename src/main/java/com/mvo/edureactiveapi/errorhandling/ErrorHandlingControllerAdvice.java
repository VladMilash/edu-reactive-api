package com.mvo.edureactiveapi.errorhandling;

import com.mvo.edureactiveapi.exeption.AlReadyExistException;
import com.mvo.edureactiveapi.exeption.ErrorResponse;
import com.mvo.edureactiveapi.exeption.NotFoundEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestControllerAdvice
@Slf4j
public class ErrorHandlingControllerAdvice {
    @ExceptionHandler(NotFoundEntityException.class)
    public Mono<ResponseEntity<ErrorResponse>> onNotFoundEntityException(NotFoundEntityException e, ServerHttpRequest request) {
        var errorResponse = errorResponseBuilder(request, 404, "Not Found", e);
        log.info("Sending errorResponse");
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(errorResponse));
    }

    @ExceptionHandler(AlReadyExistException.class)
    public Mono<ResponseEntity<ErrorResponse>> onAlreadyExistException(AlReadyExistException e, ServerHttpRequest request) {
        var errorResponse = errorResponseBuilder(request, 400, "Bad Request", e);
        log.info("Sending errorResponse");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorResponse));
    }

    private static ErrorResponse errorResponseBuilder(ServerHttpRequest request, Integer status, String error, Exception e) {
        String path = request.getURI().getPath();
        return new ErrorResponse(
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()),
            status,
            error,
            e.getMessage(),
            path
        );
    }
}
