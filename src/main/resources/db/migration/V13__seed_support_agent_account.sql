-- Compte de démo supplémentaire pour l'agent support.
-- Mot de passe : "Support123" (hash BCrypt ci-dessous).
-- ⚠ Données de démo uniquement — ne jamais utiliser ce compte en production.
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status) VALUES
    ('55555555-5555-5555-5555-555555555555', 'supportagent@gmail.com',
     '$2a$10$T/uSFU8lSFmY3iMzmKoGHu39sFfIdi3u1HnF012cXY7Tc8FD4pIjm', 'Support', 'Agent', 'SUPPORT_AGENT', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;
