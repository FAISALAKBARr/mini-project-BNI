package org.example.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){
        return status;
    }
}

//Username dobel atau password terlalu pendek perlu dijawab dengan status yang tepat, bukan sekadar "error".
// Service melempar ApiException yang membawa status dan pesan, lalu satu class pusat mengubahnya jadi respons HTTP. Controller tidak perlu try/catch lagi.
