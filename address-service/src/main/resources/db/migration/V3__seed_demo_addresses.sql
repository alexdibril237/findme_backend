-- Jeu de données de démonstration (livrable L4). Les user_id correspondent aux comptes démo
-- seedés dans auth_db (aya = 3333..., jean = 4444...). Quota de 4 respecté, identités uniques.
INSERT INTO addresses (id, user_id, country, city, district, street, house_number, postal_code, latitude, longitude) VALUES
    ('a1111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333',
     'Cameroun', 'Douala', 'Akwa', 'Rue Joffre', '464', NULL, 4.0483, 9.7043),
    ('a2222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333',
     'Cameroun', 'Douala', 'Bonanjo', 'Boulevard de la Liberté', '12', NULL, 4.0511, 9.6890),
    ('a3333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444',
     'Cameroun', 'Yaoundé', 'Bastos', 'Rue 1750', '7', NULL, 3.8891, 11.5215)
ON CONFLICT (user_id, country, city, district, street, house_number) DO NOTHING;
