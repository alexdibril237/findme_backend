package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.UserNotFoundException;
import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.RefreshTokenRepository;
import com.geolink.findme.data.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getMyProfile(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public User updateMyProfile(UUID userId, String firstName, String lastName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> listUsers(String search, Pageable pageable) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        return userRepository.search(normalized, pageable);
    }

    @Override
    @Transactional
    public void updateUserRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setRole(newRole);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(UUID userId, AccountStatus newStatus) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setStatus(newStatus);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        if (newStatus == AccountStatus.DISABLED) {
            refreshTokenRepository.revokeAllForUser(userId);
        }
    }
}
