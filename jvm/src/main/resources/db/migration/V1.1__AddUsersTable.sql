CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name VARCHAR UNIQUE NOT NULL,
  password VARCHAR NOT NULL,
  token VARCHAR NOT NULL
);

INSERT INTO users (name, password, token)
VALUES ('test', '$2a$10$yHNtm9cSR4fJn6hQNiLW3uPfrJ.Dz3zK53AfuKePD8cm25iIur9oW', 'test');
