package com.aws.snapcloud.exception;

import com.aws.snapcloud.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.resolve(status.value()),
                ex.getBindingResult().getAllErrors().stream()
                  .map(DefaultMessageSourceResolvable::getDefaultMessage)
                  .toList());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDuplicateEntityException(
            DuplicateEntityException ex) {
        return getTemplateOfResponseEntity(ex, HttpStatus.CONFLICT);
    }

    private ResponseEntity<ErrorResponse> getTemplateOfResponseEntity(
            Throwable e,
            HttpStatus httpStatus
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                httpStatus,
                List.of(e.getMessage()));
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
