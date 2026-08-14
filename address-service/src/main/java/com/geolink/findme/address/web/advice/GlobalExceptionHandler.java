package com.geolink.findme.address.web.advice;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
import com.geolink.findme.address.domain.exception.AddressQuotaExceededException;
import com.geolink.findme.address.domain.exception.DuplicateAddressException;
import com.geolink.findme.address.domain.exception.InvalidPhotoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AddressQuotaExceededException.class)
    public ProblemDetail handleQuotaExceeded(AddressQuotaExceededException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "ADDRESS_QUOTA_EXCEEDED");
    }

    @ExceptionHandler(DuplicateAddressException.class)
    public ProblemDetail handleDuplicate(DuplicateAddressException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "DUPLICATE_ADDRESS");
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ProblemDetail handleNotFound(AddressNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "ADDRESS_NOT_FOUND");
    }

    @ExceptionHandler(AddressAccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AddressAccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, e.getMessage(), "ADDRESS_ACCESS_DENIED");
    }

    @ExceptionHandler(InvalidPhotoException.class)
    public ProblemDetail handleInvalidPhoto(InvalidPhotoException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), "INVALID_PHOTO");
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
