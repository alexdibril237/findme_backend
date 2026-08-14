package com.geolink.findme.admin.web.advice;

import com.geolink.findme.admin.domain.exception.SupportTicketNotFoundException;
import com.geolink.findme.admin.domain.exception.UpstreamServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SupportTicketNotFoundException.class)
    public ProblemDetail handleNotFound(SupportTicketNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "SUPPORT_TICKET_NOT_FOUND");
    }

    @ExceptionHandler(UpstreamServiceUnavailableException.class)
    public ProblemDetail handleUpstreamUnavailable(UpstreamServiceUnavailableException e) {
        // §2.3 : toute indisponibilité d'un service appelé doit être gérée proprement, sans planter admin-service.
        return problem(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), "UPSTREAM_SERVICE_UNAVAILABLE");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Requête invalide");
        return problem(HttpStatus.BAD_REQUEST, detail, "VALIDATION_ERROR");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), "INVALID_REQUEST");
    }

    private ProblemDetail problem(HttpStatus status, String detail, String errorCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty("errorCode", errorCode);
        return problemDetail;
    }
}
