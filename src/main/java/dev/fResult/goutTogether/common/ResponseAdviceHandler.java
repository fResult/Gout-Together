package dev.fResult.goutTogether.common;

import dev.fResult.goutTogether.common.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error -> {
              propertyToError.put(error.getField(), error.getDefaultMessage());
            });
    detail.setProperty("arguments", propertyToError);

    return ResponseEntity.of(detail).build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    var errorMessage =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));
    var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);

    logger.warn("Constraint violation: {}", errorMessage);

    return ResponseEntity.of(detail).build();
  }

  @ExceptionHandler(ValidationException.class)
  protected ResponseEntity<?> handleValidationException(ValidationException ex) {
    var detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    logger.warn("Validation error: {}", ex.getMessage());

    return ResponseEntity.of(detail).build();
  }

  @ExceptionHandler(EntityNotFoundException.class)
  protected ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException ex) {
    var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    logger.warn("Entity not found: {}", ex.getMessage());

    return ResponseEntity.of(detail).build();
  }

  @ExceptionHandler(CredentialExistsException.class)
  protected ResponseEntity<?> handleCredentialExistsException(CredentialExistsException ex) {
    var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    logger.warn("Credential exists: {}", ex.getMessage());

    return ResponseEntity.of(detail).build();
  }

  @ExceptionHandler(RefreshTokenExpiredException.class)
  protected ResponseEntity<?> handleRefreshTokenExpiredException(RefreshTokenExpiredException ex) {
    var detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    logger.warn("Refresh token expired: {}", ex.getMessage());

    return ResponseEntity.of(detail).build();
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<?> handleGlobalException(Exception ex) {
    var detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    logger.error(ex.getMessage());
    ex.printStackTrace();

    return ResponseEntity.of(detail).build();
  }
}
