package com.geolink.findme.admin.web.controller;

import com.geolink.findme.admin.application.usecase.ListSupportTicketsUseCase;
import com.geolink.findme.admin.application.usecase.UpdateSupportTicketStatusUseCase;
import com.geolink.findme.admin.domain.model.TicketStatus;
import com.geolink.findme.admin.web.dto.SupportStatusUpdateRequest;
import com.geolink.findme.admin.web.dto.SupportTicketResponse;
import com.geolink.findme.admin.web.mapper.AdminWebMapper;
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

    private final ListSupportTicketsUseCase listSupportTicketsUseCase;
    private final UpdateSupportTicketStatusUseCase updateSupportTicketStatusUseCase;
    private final AdminWebMapper mapper;

    public AdminSupportController(ListSupportTicketsUseCase listSupportTicketsUseCase,
                                   UpdateSupportTicketStatusUseCase updateSupportTicketStatusUseCase,
                                   AdminWebMapper mapper) {
        this.listSupportTicketsUseCase = listSupportTicketsUseCase;
        this.updateSupportTicketStatusUseCase = updateSupportTicketStatusUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<SupportTicketResponse> list(@RequestParam(required = false) TicketStatus statut, Pageable pageable) {
        return listSupportTicketsUseCase.execute(new ListSupportTicketsUseCase.Query(pageable, statut))
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}")
    public SupportTicketResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody SupportStatusUpdateRequest request) {
        var ticket = updateSupportTicketStatusUseCase.execute(
                new UpdateSupportTicketStatusUseCase.Command(id, request.statut()));
        return mapper.toResponse(ticket);
    }
}
