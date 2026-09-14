-- Seed users for manual API testing.
-- password_hash for both is the BCrypt hash of "password123".
INSERT INTO users (name, email, password_hash, created_at)
VALUES ('Test User', 'test@example.com', '$2a$10$cwwlxB6slWKVutQHJdyyWepQgh8IXzJLm3XmxY9sX0qd13moZWpEa', NOW());

INSERT INTO users (name, email, password_hash, created_at)
VALUES ('Second User', 'second@example.com', '$2a$10$cwwlxB6slWKVutQHJdyyWepQgh8IXzJLm3XmxY9sX0qd13moZWpEa', NOW());

-- Seed a team + project so tasks have somewhere to attach to (no team/project
-- creation endpoints exist per the plan's scope, so this is the only way in).
INSERT INTO teams (name, description, created_by, created_at)
VALUES ('Engineering', 'Core engineering team', 1, NOW());

INSERT INTO projects (name, description, team_id, owner_id, created_at)
VALUES ('Task Management Backend', 'The resume project itself', 1, 1, NOW());
