package com.geolink.findme.auth.web.controller;

import com.geolink.findme.auth.application.usecase.ForgotPasswordUseCase;
import com.geolink.findme.auth.application.usecase.LogoutUseCase;
import com.geolink.findme.auth.application.usecase.RefreshTokenUseCase;
import com.geolink.findme.auth.application.usecase.ResetPasswordUseCase;
import com.geolink.findme.auth.application.usecase.SigninUseCase;
import com.geolink.findme.auth.application.usecase.SignupUseCase;
import com.geolink.findme.auth.web.dto.AuthResponse;
import com.geolink.findme.auth.web.dto.ForgotPasswordRequest;
import com.geolink.findme.auth.web.dto.MessageResponse;
import com.geolink.findme.auth.web.dto.RefreshRequest;
import com.geolink.findme.auth.web.dto.ResetPasswordRequest;
import com.geolink.findme.auth.web.dto.SigninRequest;
import com.geolink.findme.auth.web.dto.SignupRequest;
import com.geolink.findme.auth.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final SigninUseCase signinUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UserWebMapper mapper;

    public AuthController(SignupUseCase signupUseCase, SigninUseCase signinUseCase,
                           RefreshTokenUseCase refreshTokenUseCase, LogoutUseCase logoutUseCase,
                           ForgotPasswordUseCase forgotPasswordUseCase, ResetPasswordUseCase resetPasswordUseCase,
                           UserWebMapper mapper) {
        this.signupUseCase = signupUseCase;
        this.signinUseCase = signinUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        var result = signupUseCase.execute(new SignupUseCase.Command(
                request.email(), request.motDePasse(), request.prenom(), request.nom()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toAuthResponse(result));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody SigninRequest request) {
        var result = signinUseCase.execute(new SigninUseCase.Command(request.email(), request.motDePasse()));
        return ResponseEntity.ok(mapper.toAuthResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var result = refreshTokenUseCase.execute(new RefreshTokenUseCase.Command(request.refreshToken()));
        return ResponseEntity.ok(mapper.toAuthResponse(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        logoutUseCase.execute(new LogoutUseCase.Command(request.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(new ForgotPasswordUseCase.Command(request.email()));
        return ResponseEntity.ok(new MessageResponse(
                "Si un compte existe pour cet email, un lien de réinitialisation a été envoyé"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(new ResetPasswordUseCase.Command(request.token(), request.nouveauMotDePasse()));
        return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès"));
    }
}
