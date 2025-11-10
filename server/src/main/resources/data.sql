ALTER TABLE public.members ADD COLUMN IF NOT EXISTS user_id BIGINT;

ALTER TABLE public.members DROP CONSTRAINT IF EXISTS fk_members_user;
ALTER TABLE public.members
  ADD CONSTRAINT fk_members_user
  FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.members DROP CONSTRAINT IF EXISTS uk_members_user;
ALTER TABLE public.members
  ADD CONSTRAINT uk_members_user
  UNIQUE (user_id);

INSERT INTO roles(id, code) VALUES
  (1,'ROLE_ADMIN'),
  (2,'ROLE_STAFF'),
  (3,'ROLE_GUEST')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (username, password_hash, enabled)
SELECT 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiwJz7Y1koRaSP6KTcYZF6LJx8S8e2u', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO tiers(id, name, threshold) VALUES
  (1,'Bronze',0),
  (2,'Silver',1000),
  (3,'Gold',5000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'ROLE_ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO members (full_name, phone, tier_id, user_id)
SELECT 'Admin User', '1000000000', t.id, u.id
FROM users u
JOIN tiers t ON t.id = 1
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM members WHERE user_id = u.id);

INSERT INTO balances(id, points)
SELECT m.id, 500
FROM members m
JOIN users u ON u.id = m.user_id
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM balances b WHERE b.id = m.id);

INSERT INTO rewards(id,title,description,cost,active) VALUES
  (1,'Welcome Pack','Starter reward bundle',300,true),
  (2,'Gorilla Energy','Free Gorilla',100,true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('roles_id_seq',      COALESCE((SELECT MAX(id) FROM roles),      1), true);
SELECT setval('users_id_seq',      COALESCE((SELECT MAX(id) FROM users),      1), true);
SELECT setval('tiers_id_seq',      COALESCE((SELECT MAX(id) FROM tiers),      1), true);
SELECT setval('members_id_seq',    COALESCE((SELECT MAX(id) FROM members),    1), true);
SELECT setval('rewards_id_seq',    COALESCE((SELECT MAX(id) FROM rewards),    1), true);
