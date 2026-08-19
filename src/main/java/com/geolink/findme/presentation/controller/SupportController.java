package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.SupportService;
import com.geolink.findme.presentation.dto.SupportMessageRequest;
import com.geolink.findme.presentation.dto.SupportTicketResponse;
import com.geolink.findme.presentation.mapper.SupportWebMapper;
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

    private final SupportService supportService;
    private final SupportWebMapper mapper;

    public SupportController(SupportService supportService, SupportWebMapper mapper) {
        this.supportService = supportService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody SupportMessageRequest request) {
        var ticket = supportService.createTicket(request.nom(), request.email(), request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(ticket));
    }
}
