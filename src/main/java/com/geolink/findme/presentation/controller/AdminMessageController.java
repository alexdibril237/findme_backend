package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.UserMessageService;
import com.geolink.findme.presentation.dto.SendMessageRequest;
import com.geolink.findme.presentation.dto.UserMessageResponse;
import com.geolink.findme.presentation.mapper.UserMessageWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Réservé ADMIN et SUPPORT_AGENT : envoi d'un message ciblé à un utilisateur. */
@RestController
@RequestMapping("/api/admin/messages")
@PreAuthorize("hasAnyRole('ADMIN','SUPPORT_AGENT')")
public class AdminMessageController {

    private final UserMessageService userMessageService;
    private final UserMessageWebMapper mapper;

    public AdminMessageController(UserMessageService userMessageService, UserMessageWebMapper mapper) {
        this.userMessageService = userMessageService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserMessageResponse> send(@Valid @RequestBody SendMessageRequest request) {
        var message = userMessageService.send(request.destinataireId(), request.sujet(), request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(message));
    }
}
