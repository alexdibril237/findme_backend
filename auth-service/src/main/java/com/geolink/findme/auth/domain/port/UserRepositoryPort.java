package com.geolink.findme.auth.domain.port;

import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    boolean existsByEmail(Email email);

    Optional<User> findByEmail(Email email);

    Optional<User> findById(UUID id);

    User save(User user);

    /** {@code search} filtre sur email/nom/prénom (insensible à la casse) ; {@code null} = pas de filtre. */
    Page<User> search(String search, Pageable pageable);
}