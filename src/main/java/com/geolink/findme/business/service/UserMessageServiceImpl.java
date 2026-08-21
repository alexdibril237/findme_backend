package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.UserMessageAccessDeniedException;
import com.geolink.findme.business.exception.UserMessageNotFoundException;
import com.geolink.findme.business.exception.UserNotFoundException;
import com.geolink.findme.data.entity.UserMessage;
import com.geolink.findme.data.repository.UserMessageRepository;
import com.geolink.findme.data.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserMessageServiceImpl implements UserMessageService {

    private final UserMessageRepository userMessageRepository;
    private final UserRepository userRepository;

    public UserMessageServiceImpl(UserMessageRepository userMessageRepository, UserRepository userRepository) {
        this.userMessageRepository = userMessageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserMessage send(UUID recipientId, String subject, String body) {
        if (!userRepository.existsById(recipientId)) {
            throw new UserNotFoundException(recipientId);
        }

        UserMessage message = new UserMessage();
        message.setId(UUID.randomUUID());
        message.setRecipientId(recipientId);
        message.setSubject(subject);
        message.setBody(body);
        message.setRead(false);
        message.setCreatedAt(Instant.now());

        return userMessageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserMessage> listForUser(UUID userId, Pageable pageable) {
        return userMessageRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional
    public UserMessage markRead(UUID messageId, UUID requesterId) {
        UserMessage message = userMessageRepository.findById(messageId)
                .orElseThrow(() -> new UserMessageNotFoundException(messageId));
        if (!message.belongsTo(requesterId)) {
            throw new UserMessageAccessDeniedException();
        }
        message.setRead(true);
        return userMessageRepository.save(message);
    }
}
