-- Messagerie admin -> utilisateur : jusqu'ici purement côté frontend (localStorage), donc
-- invisible d'un autre appareil/navigateur et perdue au moindre nettoyage du stockage local.
CREATE TABLE user_messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    subject      VARCHAR(150) NOT NULL,
    body         TEXT NOT NULL,
    is_read      BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_messages_recipient_id ON user_messages (recipient_id);
