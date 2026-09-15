package com.geolink.findme.unit.business.service;
import com.geolink.findme.business.service.UserMessageServiceImpl;
import com.geolink.findme.business.service.UserMessageService;

import com.geolink.findme.business.exception.UserMessageAccessDeniedException;
import com.geolink.findme.business.exception.UserMessageNotFoundException;
import com.geolink.findme.business.exception.UserNotFoundException;
import com.geolink.findme.data.entity.UserMessage;
import com.geolink.findme.data.repository.UserMessageRepository;
import com.geolink.findme.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMessageServiceTest {

    @Mock
    private UserMessageRepository userMessageRepository;
    @Mock
    private UserRepository userRepository;

    private UserMessageService userMessageService;

    @BeforeEach
    void setUp() {
        userMessageService = new UserMessageServiceImpl(userMessageRepository, userRepository);
    }

    private UserMessage existing(UUID recipientId, boolean read) {
        UserMessage message = new UserMessage();
        message.setId(UUID.randomUUID());
        message.setRecipientId(recipientId);
        message.setSubject("Sujet");
        message.setBody("Corps du message");
        message.setRead(read);
        message.setCreatedAt(Instant.now());
        return message;
    }

    // --- send ---

    @Test
    void send_cree_un_message_non_lu_pour_le_destinataire() {
        UUID recipientId = UUID.randomUUID();
        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(userMessageRepository.save(any(UserMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMessage message = userMessageService.send(recipientId, "Votre demande a été traitée", "Corps");

        assertThat(message.getRecipientId()).isEqualTo(recipientId);
        assertThat(message.isRead()).isFalse();
        assertThat(message.getSubject()).isEqualTo("Votre demande a été traitée");
    }

    @Test
    void send_leve_user_not_found_si_destinataire_inexistant() {
        UUID recipientId = UUID.randomUUID();
        when(userRepository.existsById(recipientId)).thenReturn(false);

        assertThatThrownBy(() -> userMessageService.send(recipientId, "Sujet", "Corps"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userMessageRepository, never()).save(any());
    }

    // --- listForUser ---

    @Test
    void listForUser_delegue_au_repository() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = Pageable.ofSize(10);
        Page<UserMessage> page = new PageImpl<>(List.of(existing(userId, false)));
        when(userMessageRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);

        Page<UserMessage> result = userMessageService.listForUser(userId, pageable);

        assertThat(result).isSameAs(page);
    }

    // --- markRead ---

    @Test
    void markRead_marque_le_message_comme_lu_pour_son_destinataire() {
        UUID recipientId = UUID.randomUUID();
        UserMessage message = existing(recipientId, false);
        when(userMessageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userMessageRepository.save(any(UserMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMessage result = userMessageService.markRead(message.getId(), recipientId);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void markRead_est_idempotent_si_deja_lu() {
        UUID recipientId = UUID.randomUUID();
        UserMessage message = existing(recipientId, true);
        when(userMessageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userMessageRepository.save(any(UserMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMessage result = userMessageService.markRead(message.getId(), recipientId);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void markRead_leve_not_found_si_message_inexistant() {
        UUID id = UUID.randomUUID();
        when(userMessageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userMessageService.markRead(id, UUID.randomUUID()))
                .isInstanceOf(UserMessageNotFoundException.class);
    }

    @Test
    void markRead_leve_access_denied_pour_un_autre_destinataire() {
        UserMessage message = existing(UUID.randomUUID(), false);
        when(userMessageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> userMessageService.markRead(message.getId(), UUID.randomUUID()))
                .isInstanceOf(UserMessageAccessDeniedException.class);

        verify(userMessageRepository, never()).save(any());
    }
}
