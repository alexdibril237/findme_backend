package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.UserNotFoundException;
import com.geolink.findme.auth.domain.model.AccountStatus;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Réservé ADMIN — cahier des charges §2.4 ("Modifier le rôle ou désactiver un compte"). */
public class UpdateUserStatusUseCase {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public UpdateUserStatusUseCase(UserRepositoryPort userRepository, RefreshTokenRepositoryPort refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public record Command(UUID userId, AccountStatus newStatus) {
    }

    @Transactional
    public User execute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (command.newStatus() == AccountStatus.DISABLED) {
            user.disable();
            refreshTokenRepository.revokeAllForUser(user.getId());
        } else {
            user.enable();
        }
        return userRepository.save(user);
    }
}
