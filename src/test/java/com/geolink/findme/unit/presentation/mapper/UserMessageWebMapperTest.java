package com.geolink.findme.unit.presentation.mapper;
import com.geolink.findme.presentation.mapper.UserMessageWebMapper;

import com.geolink.findme.data.entity.UserMessage;
import com.geolink.findme.presentation.dto.UserMessageResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMessageWebMapperTest {

    private final UserMessageWebMapper mapper = new UserMessageWebMapper();

    private UserMessage sample(boolean read) {
        UserMessage message = new UserMessage();
        message.setId(UUID.randomUUID());
        message.setRecipientId(UUID.randomUUID());
        message.setSubject("Votre demande a été traitée");
        message.setBody("Corps du message");
        message.setRead(read);
        message.setCreatedAt(Instant.now());
        return message;
    }

    @Test
    void toResponse_transporte_les_champs_d_un_message_non_lu() {
        UserMessage message = sample(false);

        UserMessageResponse response = mapper.toResponse(message);

        assertThat(response.id()).isEqualTo(message.getId());
        assertThat(response.sujet()).isEqualTo("Votre demande a été traitée");
        assertThat(response.message()).isEqualTo("Corps du message");
        assertThat(response.lu()).isFalse();
        assertThat(response.dateCreation()).isEqualTo(message.getCreatedAt());
    }

    @Test
    void toResponse_transporte_le_statut_lu() {
        UserMessage message = sample(true);

        UserMessageResponse response = mapper.toResponse(message);

        assertThat(response.lu()).isTrue();
    }
}
