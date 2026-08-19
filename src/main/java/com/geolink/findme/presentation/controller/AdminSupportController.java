package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.model.TicketStatus;
import com.geolink.findme.business.service.SupportService;
import com.geolink.findme.presentation.dto.SupportStatusUpdateRequest;
import com.geolink.findme.presentation.dto.SupportTicketResponse;
import com.geolink.findme.presentation.mapper.SupportWebMapper;
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

/** Réservé ADMIN et SUPPORT_AGENT (cahier des charges §2.3, matrice RBAC §2.4). */
@RestController
@RequestMapping("/api/admin/support")
@PreAuthorize("hasAnyRole('ADMIN','SUPPORT_AGENT')")
public class AdminSupportController {

    private final SupportService supportService;
    private final SupportWebMapper mapper;

    public AdminSupportController(SupportService supportService, SupportWebMapper mapper) {
        this.supportService = supportService;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<SupportTicketResponse> list(@RequestParam(required = false) TicketStatus statut, Pageable pageable) {
        return supportService.listTickets(pageable, statut).map(mapper::toResponse);
    }

    @PatchMapping("/{id}")
    public SupportTicketResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody SupportStatusUpdateRequest request) {
        var ticket = supportService.updateTicketStatus(id, request.statut());
        return mapper.toResponse(ticket);
    }
}
