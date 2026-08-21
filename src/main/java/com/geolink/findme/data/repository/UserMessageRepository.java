package com.geolink.findme.data.repository;

import com.geolink.findme.data.entity.UserMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserMessageRepository extends JpaRepository<UserMessage, UUID> {

    Page<UserMessage> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);
}
