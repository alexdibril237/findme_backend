package com.geolink.findme.admin.web.controller;

import com.geolink.findme.admin.application.usecase.ListUsersUseCase;
import com.geolink.findme.admin.application.usecase.UpdateUserRoleUseCase;
import com.geolink.findme.admin.application.usecase.UpdateUserStatusUseCase;
import com.geolink.findme.admin.web.dto.UpdateRoleRequest;
import com.geolink.findme.admin.web.dto.UpdateStatusRequest;
import com.geolink.findme.admin.web.dto.UserSummaryResponse;
import com.geolink.findme.admin.web.mapper.AdminWebMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
 * Réservé ADMIN (cahier des charges §2.3, §2.4). {@code /role} et {@code /status} sont des
 * extensions additives non présentes dans les endpoints figés du Projet 4 — voir dossier de
 * conception §0 pour la justification (ne casse aucun appel existant du frontend).
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserRoleUseCase updateUserRoleUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    private final AdminWebMapper mapper;

    public AdminUserController(ListUsersUseCase listUsersUseCase, UpdateUserRoleUseCase updateUserRoleUseCase,
                                UpdateUserStatusUseCase updateUserStatusUseCase, AdminWebMapper mapper) {
        this.listUsersUseCase = listUsersUseCase;
        this.updateUserRoleUseCase = updateUserRoleUseCase;
        this.updateUserStatusUseCase = updateUserStatusUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<UserSummaryResponse> list(@RequestParam(required = false) String search, Pageable pageable) {
        return listUsersUseCase.execute(new ListUsersUseCase.Query(search, pageable)).map(mapper::toResponse);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        updateUserRoleUseCase.execute(new UpdateUserRoleUseCase.Command(id, request.role()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        updateUserStatusUseCase.execute(new UpdateUserStatusUseCase.Command(id, request.statut()));
        return ResponseEntity.noContent().build();
    }
}
