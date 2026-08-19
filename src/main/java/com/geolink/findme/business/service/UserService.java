package com.geolink.findme.business.service;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    User getMyProfile(UUID userId);

    User updateMyProfile(UUID userId, String firstName, String lastName);

    /** {@code search} filtre sur email/nom/prénom (insensible à la casse) ; {@code null}/vide = pas de filtre. */
    Page<User> listUsers(String search, Pageable pageable);

    /** Réservé ADMIN — cahier des charges §2.4 ("Modifier le rôle ou désactiver un compte"). */
    void updateUserRole(UUID userId, Role newRole);

    /** Réservé ADMIN — cahier des charges §2.4 ("Modifier le rôle ou désactiver un compte"). */
    void updateUserStatus(UUID userId, AccountStatus newStatus);
}
