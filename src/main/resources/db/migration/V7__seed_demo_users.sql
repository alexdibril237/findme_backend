-- Jeu de données de démonstration (livrable L4) pour la soutenance.
-- Mot de passe commun à tous les comptes : "Password1" (hash BCrypt ci-dessous).
-- ⚠ Données de démo uniquement — ne jamais utiliser ces comptes en production.
-- Les UUID sont figés pour être référencés par le seed des adresses (V8).
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status) VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin@geolink.africa',
     '$2y$10$J.H1Tr52bT/lPYpZyN/AsuWb7gY2dYoXocVw1.UmjUOpn4.723jtW', 'Admin', 'GeoLink', 'ADMIN', 'ACTIVE'),
    ('22222222-2222-2222-2222-222222222222', 'support@geolink.africa',
     '$2y$10$J.H1Tr52bT/lPYpZyN/AsuWb7gY2dYoXocVw1.UmjUOpn4.723jtW', 'Sara', 'Support', 'SUPPORT_AGENT', 'ACTIVE'),
    ('33333333-3333-3333-3333-333333333333', 'aya@example.com',
     '$2y$10$J.H1Tr52bT/lPYpZyN/AsuWb7gY2dYoXocVw1.UmjUOpn4.723jtW', 'Aya', 'Ndiaye', 'USER', 'ACTIVE'),
    ('44444444-4444-4444-4444-444444444444', 'jean@example.com',
     '$2y$10$J.H1Tr52bT/lPYpZyN/AsuWb7gY2dYoXocVw1.UmjUOpn4.723jtW', 'Jean', 'Mballa', 'USER', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;
