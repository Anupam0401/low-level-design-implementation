package implement.lld.exception.handler;

import implement.lld.dto.response.ErrorResponseDto;
import implement.lld.exception.InvalidExpenseTypeException;
import implement.lld.exception.InvalidSplitException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 */
@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    /**
     * Handle InvalidSplitException
     * @param ex the exception
     * @param request the web request
     * @return error response
     */
    @ExceptionHandler(InvalidSplitException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidSplitException(InvalidSplitException ex, WebRequest request) {
        log.error("Invalid split: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle InvalidExpenseTypeException
     * @param ex the exception
     * @param request the web request
     * @return error response
     */
    @ExceptionHandler(InvalidExpenseTypeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidExpenseTypeException(InvalidExpenseTypeException ex, WebRequest request) {
        log.error("Invalid expense type: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     * @param ex the exception
     * @param request the web request
     * @return error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors
     * @param ex the exception
     * @param request the web request
     * @return error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation error: {}", errors);
        
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation error")
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(Map.of("errors", errors, "errorResponse", errorResponse), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions
     * @param ex the exception
     * @param request the web request
     * @return error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint violation: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     * @param ex the exception
     * @param request the web request
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
