package com.aech.auth.exam_backend.Exceptions;

import javax.security.auth.login.CredentialExpiredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aech.auth.exam_backend.DTOs.ApiError;
import com.aech.auth.exam_backend.DTOs.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({
      UsernameNotFoundException.class,
      BadCredentialsException.class,
      CredentialExpiredException.class,
      DisabledException.class
  })
  public ResponseEntity<ApiError> handleAuthException(Exception e, HttpServletRequest req) {
    logger.info("Exception  : {}", e.getClass().getName());
    var apiErr = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), req.getRequestURI());
    return ResponseEntity.badRequest().body(apiErr);
  }

  @ExceptionHandler(ResourceNotFoundEx.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundExcpetion(ResourceNotFoundEx ex) {
    ErrorResponse internalServerError = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, 404);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(internalServerError);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllEResponseEntity(IllegalArgumentException ex) {
    ErrorResponse internalServerErr = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, 400);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(internalServerErr);
  }
}
