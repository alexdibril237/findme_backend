package com.geolink.findme.auth.infrastructure.persistence;

import com.geolink.findme.auth.domain.model.PasswordResetToken;
import com.geolink.findme.auth.domain.port.PasswordResetTokenRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity saved = jpaRepository.save(toEntity(token));
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return PasswordResetToken.rehydrate(entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.isUsed(), entity.getCreatedAt());
    }

    private PasswordResetTokenJpaEntity toEntity(PasswordResetToken token) {
        return new PasswordResetTokenJpaEntity(token.getId(), token.getUserId(), token.getTokenHash(),
                token.getExpiresAt(), token.isUsed(), token.getCreatedAt());
    }
}
