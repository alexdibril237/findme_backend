package com.geolink.findme.presentation.advice;

import com.geolink.findme.business.exception.AccountDisabledException;
import com.geolink.findme.business.exception.AddressAccessDeniedException;
import com.geolink.findme.business.exception.AddressNotFoundException;
import com.geolink.findme.business.exception.AddressQuotaExceededException;
import com.geolink.findme.business.exception.DuplicateAddressException;
import com.geolink.findme.business.exception.EmailAlreadyUsedException;
import com.geolink.findme.business.exception.InvalidCredentialsException;
import com.geolink.findme.business.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.business.exception.InvalidPhotoException;
import com.geolink.findme.business.exception.SupportTicketNotFoundException;
import com.geolink.findme.business.exception.UserMessageAccessDeniedException;
import com.geolink.findme.business.exception.UserMessageNotFoundException;
import com.geolink.findme.business.exception.UserNotFoundException;
import com.geolink.findme.business.exception.WeakPasswordException;
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

    @ExceptionHandler(AddressQuotaExceededException.class)
    public ProblemDetail handleQuotaExceeded(AddressQuotaExceededException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "ADDRESS_QUOTA_EXCEEDED");
    }

    @ExceptionHandler(DuplicateAddressException.class)
    public ProblemDetail handleDuplicateAddress(DuplicateAddressException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "DUPLICATE_ADDRESS");
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ProblemDetail handleAddressNotFound(AddressNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "ADDRESS_NOT_FOUND");
    }

    @ExceptionHandler(AddressAccessDeniedException.class)
    public ProblemDetail handleAddressAccessDenied(AddressAccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, e.getMessage(), "ADDRESS_ACCESS_DENIED");
    }

    @ExceptionHandler(InvalidPhotoException.class)
    public ProblemDetail handleInvalidPhoto(InvalidPhotoException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), "INVALID_PHOTO");
    }

    @ExceptionHandler(SupportTicketNotFoundException.class)
    public ProblemDetail handleSupportTicketNotFound(SupportTicketNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "SUPPORT_TICKET_NOT_FOUND");
    }

    @ExceptionHandler(UserMessageNotFoundException.class)
    public ProblemDetail handleUserMessageNotFound(UserMessageNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "MESSAGE_NOT_FOUND");
    }

    @ExceptionHandler(UserMessageAccessDeniedException.class)
    public ProblemDetail handleUserMessageAccessDenied(UserMessageAccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, e.getMessage(), "MESSAGE_ACCESS_DENIED");
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
        // Filet de sécurité DB (contrainte unique ou trigger trg_enforce_address_quota).
        return problem(HttpStatus.CONFLICT, "Cette opération viole une contrainte d'intégrité", "DATA_INTEGRITY_VIOLATION");
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
