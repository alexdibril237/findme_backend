CREATE TABLE support_tickets (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(150) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    message        TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'NON_TRAITE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT ck_support_tickets_status CHECK (status IN ('NON_TRAITE', 'TRAITE'))
);

CREATE INDEX idx_support_tickets_status ON support_tickets (status);
CREATE INDEX idx_support_tickets_email ON support_tickets (email);