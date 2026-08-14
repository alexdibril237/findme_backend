package com.geolink.findme.auth.infrastructure.config;

import com.geolink.findme.auth.application.usecase.ForgotPasswordUseCase;
import com.geolink.findme.auth.application.usecase.GetMyProfileUseCase;
import com.geolink.findme.auth.application.usecase.ListUsersUseCase;
import com.geolink.findme.auth.application.usecase.LogoutUseCase;
import com.geolink.findme.auth.application.usecase.RefreshTokenUseCase;
import com.geolink.findme.auth.application.usecase.ResetPasswordUseCase;
import com.geolink.findme.auth.application.usecase.SigninUseCase;
import com.geolink.findme.auth.application.usecase.SignupUseCase;
import com.geolink.findme.auth.application.usecase.UpdateMyProfileUseCase;
import com.geolink.findme.auth.application.usecase.UpdateUserRoleUseCase;
import com.geolink.findme.auth.application.usecase.UpdateUserStatusUseCase;
import com.geolink.findme.auth.domain.port.EmailSenderPort;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.PasswordHasherPort;
import com.geolink.findme.auth.domain.port.PasswordResetTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Assemble les use cases (couche application, agnostique de Spring) via l'IoC container. */
@Configuration
public class UseCaseConfig {

    @Bean
    public SignupUseCase signupUseCase(UserRepositoryPort userRepository, RefreshTokenRepositoryPort refreshTokenRepository,
                                        PasswordHasherPort passwordHasher, JwtIssuerPort jwtIssuer,
                                        SecureTokenPort secureToken,
                                        @Value("${jwt.refresh-token-ttl-days:7}") long refreshTtlDays) {
        return new SignupUseCase(userRepository, refreshTokenRepository, passwordHasher, jwtIssuer, secureToken,
                Duration.ofDays(refreshTtlDays));
    }

    @Bean
    public SigninUseCase signinUseCase(UserRepositoryPort userRepository, RefreshTokenRepositoryPort refreshTokenRepository,
                                        PasswordHasherPort passwordHasher, JwtIssuerPort jwtIssuer,
                                        SecureTokenPort secureToken,
                                        @Value("${jwt.refresh-token-ttl-days:7}") long refreshTtlDays) {
        return new SigninUseCase(userRepository, refreshTokenRepository, passwordHasher, jwtIssuer, secureToken,
                Duration.ofDays(refreshTtlDays));
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(UserRepositoryPort userRepository,
                                                     RefreshTokenRepositoryPort refreshTokenRepository,
                                                     JwtIssuerPort jwtIssuer, SecureTokenPort secureToken,
                                                     @Value("${jwt.refresh-token-ttl-days:7}") long refreshTtlDays) {
        return new RefreshTokenUseCase(userRepository, refreshTokenRepository, jwtIssuer, secureToken,
                Duration.ofDays(refreshTtlDays));
    }

    @Bean
    public LogoutUseCase logoutUseCase(RefreshTokenRepositoryPort refreshTokenRepository, SecureTokenPort secureToken) {
        return new LogoutUseCase(refreshTokenRepository, secureToken);
    }

    @Bean
    public ForgotPasswordUseCase forgotPasswordUseCase(UserRepositoryPort userRepository,
                                                         PasswordResetTokenRepositoryPort resetTokenRepository,
                                                         SecureTokenPort secureToken, EmailSenderPort emailSender,
                                                         @Value("${jwt.reset-token-ttl-minutes:30}") long resetTtlMinutes) {
        return new ForgotPasswordUseCase(userRepository, resetTokenRepository, secureToken, emailSender,
                Duration.ofMinutes(resetTtlMinutes));
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(UserRepositoryPort userRepository,
                                                       PasswordResetTokenRepositoryPort resetTokenRepository,
                                                       RefreshTokenRepositoryPort refreshTokenRepository,
                                                       PasswordHasherPort passwordHasher, SecureTokenPort secureToken) {
        return new ResetPasswordUseCase(userRepository, resetTokenRepository, refreshTokenRepository,
                passwordHasher, secureToken);
    }

    @Bean
    public GetMyProfileUseCase getMyProfileUseCase(UserRepositoryPort userRepository) {
        return new GetMyProfileUseCase(userRepository);
    }

    @Bean
    public UpdateMyProfileUseCase updateMyProfileUseCase(UserRepositoryPort userRepository) {
        return new UpdateMyProfileUseCase(userRepository);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepositoryPort userRepository) {
        return new ListUsersUseCase(userRepository);
    }

    @Bean
    public UpdateUserRoleUseCase updateUserRoleUseCase(UserRepositoryPort userRepository) {
        return new UpdateUserRoleUseCase(userRepository);
    }

    @Bean
    public UpdateUserStatusUseCase updateUserStatusUseCase(UserRepositoryPort userRepository,
                                                             RefreshTokenRepositoryPort refreshTokenRepository) {
        return new UpdateUserStatusUseCase(userRepository, refreshTokenRepository);
    }
}
