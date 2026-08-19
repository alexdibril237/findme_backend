package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.AuthService;
import com.geolink.findme.presentation.dto.AuthResponse;
import com.geolink.findme.presentation.dto.ForgotPasswordRequest;
import com.geolink.findme.presentation.dto.MessageResponse;
import com.geolink.findme.presentation.dto.RefreshRequest;
import com.geolink.findme.presentation.dto.ResetPasswordRequest;
import com.geolink.findme.presentation.dto.SigninRequest;
import com.geolink.findme.presentation.dto.SignupRequest;
import com.geolink.findme.presentation.mapper.UserWebMapper;
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

    private final AuthService authService;
    private final UserWebMapper mapper;

    public AuthController(AuthService authService, UserWebMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        var result = authService.signup(request.email(), request.motDePasse(), request.prenom(), request.nom());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toAuthResponse(result));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody SigninRequest request) {
        var result = authService.signin(request.email(), request.motDePasse());
        return ResponseEntity.ok(mapper.toAuthResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var result = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(mapper.toAuthResponse(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "Si un compte existe pour cet email, un lien de réinitialisation a été envoyé"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.nouveauMotDePasse());
        return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès"));
    }
}
