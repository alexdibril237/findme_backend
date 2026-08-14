package com.geolink.findme.admin.domain.port;

import com.geolink.findme.admin.domain.model.AccountStatus;
import com.geolink.findme.admin.domain.model.Role;
import com.geolink.findme.admin.domain.model.UserSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuthServiceClientPort {

    /** Lève {@link com.geolink.findme.admin.domain.exception.UpstreamServiceUnavailableException} si auth-service ne répond pas. */
    Page<UserSummary> findUsersPage(String search, Pageable pageable);

    void updateUserRole(UUID userId, Role newRole);

    void updateUserStatus(UUID userId, AccountStatus newStatus);
}
