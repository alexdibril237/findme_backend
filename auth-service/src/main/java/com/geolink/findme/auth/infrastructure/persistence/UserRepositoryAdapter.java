package com.geolink.findme.auth.infrastructure.persistence;

import com.geolink.findme.auth.domain.model.AccountStatus;
import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.Role;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = jpaRepository.save(toEntity(user));
        return toDomain(saved);
    }

    @Override
    public Page<User> search(String search, Pageable pageable) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        return jpaRepository.search(normalized, pageable).map(this::toDomain);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.rehydrate(entity.getId(), new Email(entity.getEmail()), entity.getPasswordHash(),
                entity.getFirstName(), entity.getLastName(), Role.valueOf(entity.getRole()),
                AccountStatus.valueOf(entity.getStatus()), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getLastLoginAt());
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(user.getId(), user.getEmail().value(), user.getPasswordHash(),
                user.getFirstName(), user.getLastName(), user.getRole().name(), user.getStatus().name(),
                user.getCreatedAt(), user.getUpdatedAt(), user.getLastLoginAt());
    }
}
