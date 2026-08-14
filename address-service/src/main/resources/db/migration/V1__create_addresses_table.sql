CREATE TABLE addresses (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL,
    country        VARCHAR(100) NOT NULL,
    city           VARCHAR(100) NOT NULL,
    district       VARCHAR(100) NOT NULL,
    street         VARCHAR(150) NOT NULL,
    house_number   VARCHAR(30)  NOT NULL,
    postal_code    VARCHAR(20),
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    photo_url      VARCHAR(500),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_addresses_owner_identity
        UNIQUE (user_id, country, city, district, street, house_number)
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);
CREATE INDEX idx_addresses_country_city ON addresses (country, city);