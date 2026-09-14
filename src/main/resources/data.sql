-- Seed a test user for manual /api/auth/login testing.
-- password_hash is the BCrypt hash of "password123".
INSERT INTO users (name, email, password_hash, created_at)
VALUES ('Test User', 'test@example.com', '$2a$10$cwwlxB6slWKVutQHJdyyWepQgh8IXzJLm3XmxY9sX0qd13moZWpEa', NOW());
