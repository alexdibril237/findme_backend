-- Jeu de données de démonstration (livrable L4) : tickets de support, statuts variés
-- pour illustrer le filtrage NON_TRAITE / TRAITE côté endpoints admin.
INSERT INTO support_tickets (id, name, email, message, status) VALUES
    ('b1111111-1111-1111-1111-111111111111', 'Aya Ndiaye', 'aya@example.com',
     'Comment ajouter une photo à mon adresse ?', 'NON_TRAITE'),
    ('b2222222-2222-2222-2222-222222222222', 'Jean Mballa', 'jean@example.com',
     'Je ne reçois pas le mail de réinitialisation.', 'NON_TRAITE'),
    ('b3333333-3333-3333-3333-333333333333', 'Awa Traoré', 'awa@example.com',
     'Merci pour votre aide, problème résolu.', 'TRAITE')
ON CONFLICT (id) DO NOTHING;
