package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

class ErrorHandlerTest {
    private ErrorHandler errorHandler;

    @BeforeEach
    public void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    public void handleUnsupportedStateException_ReturnsInternalServerError() {
        UnsupportedStateException ex = new UnsupportedStateException("Unsupported state");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleUnsupportedStateException(ex);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void handleUnsupportedStateException_ReturnsErrorResponseWithErrorMessage() {
        UnsupportedStateException ex = new UnsupportedStateException("Unsupported state");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleUnsupportedStateException(ex);
        ErrorResponse errorResponse = responseEntity.getBody();

        Assertions.assertNotNull(errorResponse);
        Assertions.assertEquals("Unsupported state", errorResponse.getError());
    }

    @Test
    public void createErrorResponse_WithErrorMessage() {
        ErrorResponse errorResponse = new ErrorResponse("Error message");

        Assertions.assertEquals("Error message", errorResponse.getError());
    }

    @Test
    public void createNotFoundBookingException_WithMessage() {
        String message = "Booking not found";
        NotFoundBookingException exception = new NotFoundBookingException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void notFoundBookingException_HasResponseStatusNotFound() {
        ResponseStatus responseStatus = NotFoundBookingException.class.getAnnotation(ResponseStatus.class);

        Assertions.assertNotNull(responseStatus);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

    @Test
    public void createNotFoundItemException_WithMessage() {
        String message = "Item not found";
        NotFoundItemException exception = new NotFoundItemException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void notFoundItemException_HasResponseStatusNotFound() {
        ResponseStatus responseStatus = NotFoundItemException.class.getAnnotation(ResponseStatus.class);

        Assertions.assertNotNull(responseStatus);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

    @Test
    public void createNotFoundUserException_WithMessage() {
        String message = "User not found";
        NotFoundUserException exception = new NotFoundUserException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void notFoundUserException_HasResponseStatusNotFound() {
        ResponseStatus responseStatus = NotFoundUserException.class.getAnnotation(ResponseStatus.class);

        Assertions.assertNotNull(responseStatus);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

    @Test
    public void createRequestNotFoundException_WithMessage() {
        String message = "Request not found";
        RequestNotFoundException exception = new RequestNotFoundException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void requestNotFoundException_HasResponseStatusNotFound() {
        ResponseStatus responseStatus = RequestNotFoundException.class.getAnnotation(ResponseStatus.class);

        Assertions.assertNotNull(responseStatus);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

    @Test
    public void createUnsupportedStateException_WithMessage() {
        String message = "Unsupported state";
        UnsupportedStateException exception = new UnsupportedStateException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}