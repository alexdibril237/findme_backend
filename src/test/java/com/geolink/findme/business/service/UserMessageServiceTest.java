package com.geolink.findme.business.service;

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
import org.springframework.data.domain.PageRequest;
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

    private UserMessage existing(UUID recipientId) {
        UserMessage message = new UserMessage();
        message.setId(UUID.randomUUID());
        message.setRecipientId(recipientId);
        message.setSubject("Votre demande a été traitée");
        message.setBody("Bonjour, ...");
        message.setRead(false);
        message.setCreatedAt(Instant.now());
        return message;
    }

    // --- send ---

    @Test
    void envoie_un_message_a_un_destinataire_existant() {
        UUID recipientId = UUID.randomUUID();
        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(userMessageRepository.save(any(UserMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMessage sent = userMessageService.send(recipientId, "Sujet", "Corps du message");

        assertThat(sent.getRecipientId()).isEqualTo(recipientId);
        assertThat(sent.getSubject()).isEqualTo("Sujet");
        assertThat(sent.isRead()).isFalse();
        verify(userMessageRepository).save(any(UserMessage.class));
    }

    @Test
    void refuse_d_envoyer_a_un_destinataire_inexistant() {
        UUID recipientId = UUID.randomUUID();
        when(userRepository.existsById(recipientId)).thenReturn(false);

        assertThatThrownBy(() -> userMessageService.send(recipientId, "Sujet", "Corps"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userMessageRepository, never()).save(any());
    }

    // --- listForUser ---

    @Test
    void liste_les_messages_du_destinataire() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        UserMessage message = existing(userId);
        Page<UserMessage> page = new PageImpl<>(List.of(message));
        when(userMessageRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);

        Page<UserMessage> result = userMessageService.listForUser(userId, pageable);

        assertThat(result.getContent()).containsExactly(message);
    }

    // --- markRead ---

    @Test
    void marque_lu_un_message_par_son_destinataire() {
        UUID userId = UUID.randomUUID();
        UserMessage message = existing(userId);
        when(userMessageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userMessageRepository.save(any(UserMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMessage result = userMessageService.markRead(message.getId(), userId);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void marque_lu_leve_not_found_quand_message_inexistant() {
        UUID id = UUID.randomUUID();
        when(userMessageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userMessageService.markRead(id, UUID.randomUUID()))
                .isInstanceOf(UserMessageNotFoundException.class);
    }

    @Test
    void marque_lu_leve_access_denied_pour_un_autre_utilisateur() {
        UserMessage message = existing(UUID.randomUUID());
        when(userMessageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> userMessageService.markRead(message.getId(), UUID.randomUUID()))
                .isInstanceOf(UserMessageAccessDeniedException.class);

        verify(userMessageRepository, never()).save(any());
    }
}
