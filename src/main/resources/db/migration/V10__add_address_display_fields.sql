-- Les champs label / statut / code d'adresse / indicatif pays étaient jusqu'ici générés et
-- persistés uniquement côté frontend (localStorage), donc perdus d'un appareil à l'autre.
-- On les fait migrer en base pour qu'ils suivent l'adresse comme n'importe quelle autre colonne.
ALTER TABLE addresses
    ADD COLUMN label        VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN address_code  VARCHAR(30),
    ADD COLUMN country_code  VARCHAR(5);

-- Backfill des adresses de démo existantes (V8) avant de rendre address_code obligatoire/unique.
UPDATE addresses SET label = district WHERE label = '';
UPDATE addresses SET address_code = 'FM-' || upper(substr(district, 1, 4)) || '-' || upper(substr(md5(id::text), 1, 4))
WHERE address_code IS NULL;

ALTER TABLE addresses ALTER COLUMN address_code SET NOT NULL;
ALTER TABLE addresses ADD CONSTRAINT uq_addresses_address_code UNIQUE (address_code);
