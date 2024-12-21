CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    tour_id INTEGER NOT NULL REFERENCES tours(id),
    rate INTEGER NOT NULL,
    description VARCHAR(1000)
);
