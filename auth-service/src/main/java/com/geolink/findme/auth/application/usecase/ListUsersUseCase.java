package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consommé par admin-service via un appel REST interne (GET /internal/users). La ressource User
 * (cahier des charges §2.1) ne porte pas de champ pays/ville : seule la recherche par email/nom
 * (explicitement prévue par §2.3) est implémentée ici — voir docs/conception pour la décision.
 */
public class ListUsersUseCase {

    private final UserRepositoryPort userRepository;

    public ListUsersUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public record Query(String search, Pageable pageable) {
    }

    @Transactional(readOnly = true)
    public Page<User> execute(Query query) {
        return userRepository.search(query.search(), query.pageable());
    }
}