package com.geolink.findme.auth.infrastructure.persistence;

import com.geolink.findme.auth.domain.model.RefreshToken;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity saved = jpaRepository.save(toEntity(refreshToken));
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        jpaRepository.revokeAllForUser(userId);
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.rehydrate(entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.isRevoked(), entity.getCreatedAt());
    }

    private RefreshTokenJpaEntity toEntity(RefreshToken token) {
        return new RefreshTokenJpaEntity(token.getId(), token.getUserId(), token.getTokenHash(),
                token.getExpiresAt(), token.isRevoked(), token.getCreatedAt());
    }
}
