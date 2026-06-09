-- Default test user
-- Username: alice
-- Password: password123
INSERT INTO app_users (username, password_hash, created_at)
VALUES ('alice', '$2a$10$sTr0y7kEFcN7ExKs.Zj9ZO7K.FVKT9L9YQh0TZ6aHGgJRCnb0y4/a', NOW())
ON CONFLICT (username) DO NOTHING;
