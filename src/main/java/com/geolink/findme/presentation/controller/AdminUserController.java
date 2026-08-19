package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.UserService;
import com.geolink.findme.presentation.dto.UpdateRoleRequest;
import com.geolink.findme.presentation.dto.UpdateStatusRequest;
import com.geolink.findme.presentation.dto.UserProfileResponse;
import com.geolink.findme.presentation.mapper.UserWebMapper;
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
 * conception pour la justification (ne casse aucun appel existant du frontend).
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserWebMapper mapper;

    public AdminUserController(UserService userService, UserWebMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<UserProfileResponse> list(@RequestParam(required = false) String search, Pageable pageable) {
        return userService.listUsers(search, pageable).map(mapper::toProfileResponse);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        userService.updateUserRole(id, request.role());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        userService.updateUserStatus(id, request.statut());
        return ResponseEntity.noContent().build();
    }
}
