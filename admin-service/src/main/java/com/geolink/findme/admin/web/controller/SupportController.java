package com.geolink.findme.admin.web.controller;

import com.geolink.findme.admin.application.usecase.CreateSupportTicketUseCase;
import com.geolink.findme.admin.web.dto.SupportMessageRequest;
import com.geolink.findme.admin.web.dto.SupportTicketResponse;
import com.geolink.findme.admin.web.mapper.AdminWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Formulaire public (§2.3) : aucune authentification requise (voir SecurityConfig). */
@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final CreateSupportTicketUseCase createSupportTicketUseCase;
    private final AdminWebMapper mapper;

    public SupportController(CreateSupportTicketUseCase createSupportTicketUseCase, AdminWebMapper mapper) {
        this.createSupportTicketUseCase = createSupportTicketUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody SupportMessageRequest request) {
        var ticket = createSupportTicketUseCase.execute(new CreateSupportTicketUseCase.Command(
                request.nom(), request.email(), request.message()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(ticket));
    }
}
