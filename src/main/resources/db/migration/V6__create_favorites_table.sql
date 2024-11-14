CREATE IF NOT EXISTS favorites(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    tour_id INTEGER NOT NULL REFERENCES tours(id),
);
