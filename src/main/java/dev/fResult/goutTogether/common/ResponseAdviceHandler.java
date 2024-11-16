package dev.fResult.goutTogether.common;

import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;

@RestControllerAdvice
public class ResponseAdviceHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseAdviceHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        var propertyToError = new HashMap<String, Object>();
        var detail = ProblemDetail.forStatusAndDetail(status, "Invalid request arguments");

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            propertyToError.put(
                    error.getField(),
                    error.getDefaultMessage());
        });
        detail.setProperty("arguments", propertyToError);

        return ResponseEntity.of(detail).build();
    }

    @ExceptionHandler(EntityNotFound.class)
    protected ResponseEntity<?> handleEntityNotFoundException(EntityNotFound ex) {
        var detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
        logger.info("Entity not found: {}", ex.getMessage());

        return ResponseEntity.of(detail).build();
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleGlobalException(Exception ex) {
        var detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
        logger.error(ex.getMessage());

        return ResponseEntity.of(detail).build();
    }
}