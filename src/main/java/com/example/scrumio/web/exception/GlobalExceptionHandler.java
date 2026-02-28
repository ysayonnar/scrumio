package com.example.scrumio.web.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // TODO: just an example
//    @ExceptionHandler(TicketNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleTicketNotFound(TicketNotFoundException ex) {
//        ErrorResponse error = new ErrorResponse(
//                "TICKET_NOT_FOUND",
//                ex.getMessage()
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(error);
//    }
}
