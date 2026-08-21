package com.geolink.findme.business.service;

import com.geolink.findme.data.entity.UserMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserMessageService {

    /** Réservé ADMIN/SUPPORT_AGENT — notifie un utilisateur précis (destinataire vérifié). */
    UserMessage send(UUID recipientId, String subject, String body);

    Page<UserMessage> listForUser(UUID userId, Pageable pageable);

    UserMessage markRead(UUID messageId, UUID requesterId);
}
