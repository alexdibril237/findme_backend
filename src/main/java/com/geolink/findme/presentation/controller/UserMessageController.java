package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.UserMessageService;
import com.geolink.findme.presentation.dto.UserMessageResponse;
import com.geolink.findme.presentation.mapper.UserMessageWebMapper;
import com.geolink.findme.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Boîte de réception de l'utilisateur courant (messages envoyés par l'admin/support). */
@RestController
@RequestMapping("/api/messages")
public class UserMessageController {

    private final UserMessageService userMessageService;
    private final CurrentUserProvider currentUserProvider;
    private final UserMessageWebMapper mapper;

    public UserMessageController(UserMessageService userMessageService, CurrentUserProvider currentUserProvider,
                                  UserMessageWebMapper mapper) {
        this.userMessageService = userMessageService;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<UserMessageResponse> list(Pageable pageable) {
        return userMessageService.listForUser(currentUserProvider.requireCurrentUserId(), pageable)
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/read")
    public UserMessageResponse markRead(@PathVariable UUID id) {
        var message = userMessageService.markRead(id, currentUserProvider.requireCurrentUserId());
        return mapper.toResponse(message);
    }
}
