-- Filet de sécurité au niveau base de données : la règle des 4 adresses maximum
-- est d'abord vérifiée par le use case applicatif (message d'erreur 409 explicite),
-- mais elle est également garantie ici pour empêcher tout contournement direct de la base.
CREATE OR REPLACE FUNCTION enforce_address_quota()
    RETURNS TRIGGER AS $$
DECLARE
    current_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO current_count
    FROM addresses
    WHERE user_id = NEW.user_id;

    IF current_count >= 4 THEN
        RAISE EXCEPTION 'ADDRESS_QUOTA_EXCEEDED: user % already owns 4 addresses', NEW.user_id
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_address_quota
    BEFORE INSERT ON addresses
    FOR EACH ROW
    EXECUTE FUNCTION enforce_address_quota();