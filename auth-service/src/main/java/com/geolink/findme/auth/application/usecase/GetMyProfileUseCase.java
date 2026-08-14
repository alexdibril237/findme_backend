package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.UserNotFoundException;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetMyProfileUseCase {

    private final UserRepositoryPort userRepository;

    public GetMyProfileUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User execute(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}