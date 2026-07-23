package com.krishna.Pujamart.identity.exception;

import com.krishna.Pujamart.catalog.exception.*;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse> handleDuplicateEmailException(DuplicateEmailException ex){
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build(),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateContactException.class)
    public ResponseEntity<ApiResponse> handleDuplicateContactException(DuplicateContactException ex){
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build(),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(UserNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build(),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("Validation Failed")
                .data(errors)
                .build(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUsernameNotFound(
            UsernameNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Invalid username or password")
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("Authentication Failed")
                .data("Invalid username or password")
                .build(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            MalformedJwtException.class,
            ExpiredJwtException.class,
            SignatureException.class,
            UnsupportedJwtException.class
    })
    public ResponseEntity<ApiResponse<?>> handleJwtExceptions(Exception ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Invalid or expired JWT token")
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Request body is missing or invalid")
                        .build());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaType(
            HttpMediaTypeNotSupportedException ex) {

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Content-Type must be application/json")
                                .build()
                );
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleRefreshTokenException(RefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFound(
            NoHandlerFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("API endpoint not found")
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Access denied: You do not have permission to access this resource")
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );

        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handlePasswordMismatchException(PasswordMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler({ CategoryNotFoundException.class,
                        DeityNotFoundException.class,
                        ProductNotFoundException.class })
    public ResponseEntity<ApiResponse<?>> handleItemNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler({ CategoryAlreadyExistsException.class,
                        DeityAlreadyExistsException.class })
    public ResponseEntity<ApiResponse<?>> handleItemAlreadyExists(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler({ CategoryInUseException.class,
                        DeityInUseException.class})
    public ResponseEntity<ApiResponse<?>> handleItemInUse(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateSkuException(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(InvalidDiscountPriceException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidDiscountPrice(
            InvalidDiscountPriceException ex) {

        return new ResponseEntity<>(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ProductVariantNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleProductVariantNotFound(
            ProductVariantNotFoundException ex) {

        return new ResponseEntity<>(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleInternalServerError(Exception ex) {
        log.error("Internal Server Error : "+ex);
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("Internal Server Error")
                .build(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
