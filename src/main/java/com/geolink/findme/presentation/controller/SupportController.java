package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.SupportService;
import com.geolink.findme.presentation.dto.SupportMessageRequest;
import com.geolink.findme.presentation.dto.SupportTicketResponse;
import com.geolink.findme.presentation.mapper.SupportWebMapper;
import com.geolink.findme.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Formulaire public (§2.3) : aucune authentification requise (voir SecurityConfig). Si la
 * requete porte tout de meme un token valide (utilisateur connecte), le ticket est relie a son
 * compte pour permettre la notification automatique a la resolution (cf. SupportServiceImpl).
 */
@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;
    private final SupportWebMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public SupportController(SupportService supportService, SupportWebMapper mapper,
                              CurrentUserProvider currentUserProvider) {
        this.supportService = supportService;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody SupportMessageRequest request) {
        var ticket = supportService.createTicket(request.nom(), request.email(), request.message(),
                currentUserProvider.currentUserIdOrNull());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(ticket));
    }
}
