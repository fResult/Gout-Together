package dev.fResult.goutTogether.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ResponseAdviceHandler extends ResponseEntityExceptionHandler {
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
