package com.example.emergencyapi.controller;

import com.example.emergencyapi.dto.MessageResponse;
import com.example.emergencyapi.dto.MessageWithSuggestionsResponse;
import com.example.emergencyapi.exception.BadRequestException;
import com.example.emergencyapi.exception.NotRecognizedWithSuggestionsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleBadRequest(BadRequestException e) {
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler(NotRecognizedWithSuggestionsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageWithSuggestionsResponse handleNotRecognized(NotRecognizedWithSuggestionsException e) {
        return new MessageWithSuggestionsResponse(e.getMessage(), e.getSuggestions());
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleValidationErrors(Exception e) {
        return new MessageResponse("Invalid request parameters");
    }
}
