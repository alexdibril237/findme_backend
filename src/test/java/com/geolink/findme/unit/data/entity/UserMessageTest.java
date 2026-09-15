package com.geolink.findme.unit.data.entity;
import com.geolink.findme.data.entity.UserMessage;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMessageTest {

    @Test
    void belongsTo_vrai_pour_le_destinataire() {
        UUID recipient = UUID.randomUUID();
        UserMessage message = new UserMessage();
        message.setRecipientId(recipient);

        assertThat(message.belongsTo(recipient)).isTrue();
    }

    @Test
    void belongsTo_faux_pour_un_autre_utilisateur() {
        UserMessage message = new UserMessage();
        message.setRecipientId(UUID.randomUUID());

        assertThat(message.belongsTo(UUID.randomUUID())).isFalse();
    }
}
