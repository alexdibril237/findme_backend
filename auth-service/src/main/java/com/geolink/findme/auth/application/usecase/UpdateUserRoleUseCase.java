package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.UserNotFoundException;
import com.geolink.findme.auth.domain.model.Role;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Réservé ADMIN — cahier des charges §2.4 ("Modifier le rôle ou désactiver un compte"). */
public class UpdateUserRoleUseCase {

    private final UserRepositoryPort userRepository;

    public UpdateUserRoleUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public record Command(UUID userId, Role newRole) {
    }

    @Transactional
    public User execute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        user.changeRole(command.newRole());
        return userRepository.save(user);
    }
}
