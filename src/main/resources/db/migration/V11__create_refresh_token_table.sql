CREATE TABLE IF NOT EXISTS refresh_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(3000) NOT NULL,
    issued_date TIMESTAMP WITH TIME ZONE NOT NULL,
    usage VARCHAR(7) NOT NULL,
    resource_id INTEGER NOT NULL,
    is_expired BOOLEAN NOT NULL
);
