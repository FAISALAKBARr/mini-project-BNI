package org.example.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<String> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}

//@RestControllerAdvice berlaku untuk semua controller: setiap ApiException yang dilempar dari mana pun otomatis jadi respons dengan status dan pesannya.
// extends RuntimeException dipakai supaya tidak perlu deklarasi throws di setiap method.
