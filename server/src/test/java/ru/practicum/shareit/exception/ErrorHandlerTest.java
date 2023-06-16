package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        NotFoundBookingException ex = new NotFoundBookingException("Booking not found");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleNotFoundBookingException(ex);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void createNotFoundItemException_WithMessage() {
        String message = "Item not found";
        NotFoundItemException exception = new NotFoundItemException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void notFoundItemException_HasResponseStatusNotFound() {
        NotFoundItemException ex = new NotFoundItemException("Item not found");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleNotFoundItemException(ex);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void createNotFoundUserException_WithMessage() {
        String message = "User not found";
        NotFoundUserException exception = new NotFoundUserException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void notFoundUserException_HasResponseStatusNotFound() {
        NotFoundUserException ex = new NotFoundUserException("Item not found");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleNotFoundUserException(ex);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void createRequestNotFoundException_WithMessage() {
        String message = "Request not found";
        RequestNotFoundException exception = new RequestNotFoundException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void requestNotFoundException_HasResponseStatusNotFound() {
        RequestNotFoundException ex = new RequestNotFoundException("Item not found");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleRequestNotFoundException(ex);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void createUnsupportedStateException_WithMessage() {
        String message = "Unsupported state";
        UnsupportedStateException exception = new UnsupportedStateException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void createBadRequestException_WithMessage() {
        String message = "Bad request.";
        BadRequestException exception = new BadRequestException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void badRequestException_HasResponseStatusBad_Request() {
        BadRequestException ex = new BadRequestException("Bad request.");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handlerBadRequestException(ex);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}