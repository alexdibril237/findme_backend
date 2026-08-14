package com.geolink.findme.auth.web.advice;

import com.geolink.findme.auth.domain.exception.AccountDisabledException;
import com.geolink.findme.auth.domain.exception.EmailAlreadyUsedException;
import com.geolink.findme.auth.domain.exception.InvalidCredentialsException;
import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.exception.UserNotFoundException;
import com.geolink.findme.auth.domain.exception.WeakPasswordException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail handleEmailAlreadyUsed(EmailAlreadyUsedException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "EMAIL_ALREADY_USED");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, e.getMessage(), "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ProblemDetail handleAccountDisabled(AccountDisabledException e) {
        return problem(HttpStatus.FORBIDDEN, e.getMessage(), "ACCOUNT_DISABLED");
    }

    @ExceptionHandler(InvalidOrExpiredTokenException.class)
    public ProblemDetail handleInvalidOrExpiredToken(InvalidOrExpiredTokenException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), "INVALID_OR_EXPIRED_TOKEN");
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ProblemDetail handleWeakPassword(WeakPasswordException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), "WEAK_PASSWORD");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "USER_NOT_FOUND");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Requête invalide");
        return problem(HttpStatus.BAD_REQUEST, detail, "VALIDATION_ERROR");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // Filet de sécurité : contrainte unique violée malgré la vérification applicative
        // (ex. deux inscriptions concurrentes avec le même email).
        return problem(HttpStatus.CONFLICT, "Cette ressource existe déjà", "DATA_INTEGRITY_VIOLATION");
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
