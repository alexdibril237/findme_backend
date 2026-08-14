package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.UserNotFoundException;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateMyProfileUseCase {

    private final UserRepositoryPort userRepository;

    public UpdateMyProfileUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public record Command(UUID userId, String firstName, String lastName) {
    }

    @Transactional
    public User execute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        user.updateProfile(command.firstName(), command.lastName());
        return userRepository.save(user);
    }
}