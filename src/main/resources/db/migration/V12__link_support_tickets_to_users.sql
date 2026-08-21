-- Permet de relier un ticket de support au compte de son auteur quand il est connecte
-- (formulaire public utilisable sans compte, cf. V6), afin de pouvoir le notifier
-- automatiquement quand l'admin/support resout son ticket.
ALTER TABLE support_tickets ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_support_tickets_user_id ON support_tickets (user_id);
