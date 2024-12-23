package it.linksmt.rental.exception;

import it.linksmt.rental.dto.ErrorResponse;
import it.linksmt.rental.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle our custom ServiceException and its subclasses
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleRentalApiException(
            ServiceException ex,
            WebRequest request) {

        HttpStatus httpStatus = getHttpStatusForErrorCode(ex.getErrorCode());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        // Collect all validation errors into a single message
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Validation failed: " + validationErrors)
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private HttpStatus getHttpStatusForErrorCode(ErrorCode errorCode) {
        return switch (errorCode.getCode()) {
            case 101 -> HttpStatus.UNAUTHORIZED;     // INVALID_CREDENTIALS
            case 102 -> HttpStatus.UNAUTHORIZED;     // INVALID_TOKEN
            case 103 -> HttpStatus.FORBIDDEN;        // UNAUTHORIZED_ACCESS

            case 201 -> HttpStatus.NOT_FOUND;        // USER_NOT_FOUND
            case 202, 602-> HttpStatus.CONFLICT;         // USER_ALREADY_EXISTS

            case 301 -> HttpStatus.NOT_FOUND;        // VEHICLE_NOT_FOUND
            case 302 -> HttpStatus.CONFLICT;         // VEHICLE_NOT_AVAILABLE

            case 401, 402 -> HttpStatus.BAD_REQUEST; // VALIDATION_ERROR, BAD_REQUEST

            case 501 -> HttpStatus.INTERNAL_SERVER_ERROR; // INTERNAL_SERVER_ERROR

            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}