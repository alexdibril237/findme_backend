package com.geolink.findme.unit.presentation.advice;
import com.geolink.findme.presentation.advice.GlobalExceptionHandler;

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
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private void assertProblem(ProblemDetail problem, HttpStatus status, String errorCode) {
        assertThat(problem.getStatus()).isEqualTo(status.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", errorCode);
    }

    @Test
    void handleEmailAlreadyUsed_retourne_409() {
        ProblemDetail problem = handler.handleEmailAlreadyUsed(new EmailAlreadyUsedException("jane@doe.io"));
        assertProblem(problem, HttpStatus.CONFLICT, "EMAIL_ALREADY_USED");
    }

    @Test
    void handleInvalidCredentials_retourne_401() {
        ProblemDetail problem = handler.handleInvalidCredentials(new InvalidCredentialsException());
        assertProblem(problem, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    @Test
    void handleAccountDisabled_retourne_403() {
        ProblemDetail problem = handler.handleAccountDisabled(new AccountDisabledException());
        assertProblem(problem, HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
    }

    @Test
    void handleInvalidOrExpiredToken_retourne_400() {
        ProblemDetail problem = handler.handleInvalidOrExpiredToken(new InvalidOrExpiredTokenException("Token expiré"));
        assertProblem(problem, HttpStatus.BAD_REQUEST, "INVALID_OR_EXPIRED_TOKEN");
        assertThat(problem.getDetail()).isEqualTo("Token expiré");
    }

    @Test
    void handleWeakPassword_retourne_400() {
        ProblemDetail problem = handler.handleWeakPassword(new WeakPasswordException());
        assertProblem(problem, HttpStatus.BAD_REQUEST, "WEAK_PASSWORD");
    }

    @Test
    void handleUserNotFound_retourne_404() {
        ProblemDetail problem = handler.handleUserNotFound(new UserNotFoundException(UUID.randomUUID()));
        assertProblem(problem, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    @Test
    void handleQuotaExceeded_retourne_409() {
        ProblemDetail problem = handler.handleQuotaExceeded(new AddressQuotaExceededException(4));
        assertProblem(problem, HttpStatus.CONFLICT, "ADDRESS_QUOTA_EXCEEDED");
    }

    @Test
    void handleDuplicateAddress_retourne_409() {
        ProblemDetail problem = handler.handleDuplicateAddress(new DuplicateAddressException());
        assertProblem(problem, HttpStatus.CONFLICT, "DUPLICATE_ADDRESS");
    }

    @Test
    void handleAddressNotFound_retourne_404() {
        ProblemDetail problem = handler.handleAddressNotFound(new AddressNotFoundException(UUID.randomUUID()));
        assertProblem(problem, HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND");
    }

    @Test
    void handleAddressAccessDenied_retourne_403() {
        ProblemDetail problem = handler.handleAddressAccessDenied(new AddressAccessDeniedException());
        assertProblem(problem, HttpStatus.FORBIDDEN, "ADDRESS_ACCESS_DENIED");
    }

    @Test
    void handleInvalidPhoto_retourne_400() {
        ProblemDetail problem = handler.handleInvalidPhoto(new InvalidPhotoException("Fichier trop volumineux"));
        assertProblem(problem, HttpStatus.BAD_REQUEST, "INVALID_PHOTO");
    }

    @Test
    void handleSupportTicketNotFound_retourne_404() {
        ProblemDetail problem = handler.handleSupportTicketNotFound(new SupportTicketNotFoundException(UUID.randomUUID()));
        assertProblem(problem, HttpStatus.NOT_FOUND, "SUPPORT_TICKET_NOT_FOUND");
    }

    @Test
    void handleUserMessageNotFound_retourne_404() {
        ProblemDetail problem = handler.handleUserMessageNotFound(new UserMessageNotFoundException(UUID.randomUUID()));
        assertProblem(problem, HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND");
    }

    @Test
    void handleUserMessageAccessDenied_retourne_403() {
        ProblemDetail problem = handler.handleUserMessageAccessDenied(new UserMessageAccessDeniedException());
        assertProblem(problem, HttpStatus.FORBIDDEN, "MESSAGE_ACCESS_DENIED");
    }

    @Test
    void handleDataIntegrityViolation_retourne_409() {
        ProblemDetail problem = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("contrainte violée"));
        assertProblem(problem, HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION");
    }

    @Test
    void handleIllegalArgument_retourne_400() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("Latitude invalide"));
        assertProblem(problem, HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        assertThat(problem.getDetail()).isEqualTo("Latitude invalide");
    }

    @Test
    void handleValidation_agrege_les_erreurs_de_champ() {
        FieldError fieldError1 = new FieldError("signupRequest", "email", "doit être une adresse email valide");
        FieldError fieldError2 = new FieldError("signupRequest", "password", "ne doit pas être vide");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail problem = handler.handleValidation(exception);

        assertProblem(problem, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        assertThat(problem.getDetail())
                .contains("email: doit être une adresse email valide")
                .contains("password: ne doit pas être vide");
    }

    @Test
    void handleValidation_utilise_un_message_par_defaut_sans_erreur_de_champ() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail problem = handler.handleValidation(exception);

        assertThat(problem.getDetail()).isEqualTo("Requête invalide");
    }
}
