package com.pragma.ms_capacidades.infrastructure.exception;

import com.pragma.ms_capacidades.domain.exception.BadRequestException;
import com.pragma.ms_capacidades.domain.exception.CapacityAlreadyExistsException;
import com.pragma.ms_capacidades.domain.exception.InvalidCapacityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(
            WebExchangeBindException ex) {

        List<String> errorMessages = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));

        ErrorResponse error = ErrorResponse.builder()
                .errors(errorMessages).code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now()).build();

        return buildResponse(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(CapacityAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTechnologyAlreadyExists(
            CapacityAlreadyExistsException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.CONFLICT.value())
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now()).build();

        return buildResponse(HttpStatus.CONFLICT, error);
    }

    @ExceptionHandler(InvalidCapacityException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTechnologyAlreadyExists(
            InvalidCapacityException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now()).build();

        return buildResponse(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTechnologyAlreadyExists(
            InvalidSortFieldException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now()).build();

        return buildResponse(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTechnologyAlreadyExists(
            BadRequestException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .errors(Arrays.asList(ex.getMessage().split("\\|")))
                .timestamp(LocalDateTime.now()).build();

        return buildResponse(HttpStatus.BAD_REQUEST, error);
    }

    private Mono<ResponseEntity<ErrorResponse>> buildResponse(HttpStatus status, ErrorResponse error) {
        return Mono.just(ResponseEntity
                .status(status)
                .body(error)
        );
    }
}
