package com.geolink.findme.auth.web.controller;

import com.geolink.findme.auth.application.usecase.ListUsersUseCase;
import com.geolink.findme.auth.application.usecase.UpdateUserRoleUseCase;
import com.geolink.findme.auth.application.usecase.UpdateUserStatusUseCase;
import com.geolink.findme.auth.web.dto.UpdateRoleRequest;
import com.geolink.findme.auth.web.dto.UpdateStatusRequest;
import com.geolink.findme.auth.web.dto.UserProfileResponse;
import com.geolink.findme.auth.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints internes, consommés uniquement par admin-service (jamais exposés par l'API Gateway
 * publique — voir docs/conception §2.5). Servent de support aux endpoints publics
 * {@code /api/admin/users} et à l'extension additive {@code /api/admin/users/{id}/role|status}.
 */
@RestController
@RequestMapping("/internal/users")
@PreAuthorize("hasRole('ADMIN')")
public class InternalUserController {

    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserRoleUseCase updateUserRoleUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    private final UserWebMapper mapper;

    public InternalUserController(ListUsersUseCase listUsersUseCase, UpdateUserRoleUseCase updateUserRoleUseCase,
                                   UpdateUserStatusUseCase updateUserStatusUseCase, UserWebMapper mapper) {
        this.listUsersUseCase = listUsersUseCase;
        this.updateUserRoleUseCase = updateUserRoleUseCase;
        this.updateUserStatusUseCase = updateUserStatusUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<UserProfileResponse> list(@RequestParam(required = false) String search, Pageable pageable) {
        return listUsersUseCase.execute(new ListUsersUseCase.Query(search, pageable)).map(mapper::toProfileResponse);
    }

    @PatchMapping("/{id}/role")
    public UserProfileResponse updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        var user = updateUserRoleUseCase.execute(new UpdateUserRoleUseCase.Command(id, request.role()));
        return mapper.toProfileResponse(user);
    }

    @PatchMapping("/{id}/status")
    public UserProfileResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        var user = updateUserStatusUseCase.execute(new UpdateUserStatusUseCase.Command(id, request.statut()));
        return mapper.toProfileResponse(user);
    }
}
