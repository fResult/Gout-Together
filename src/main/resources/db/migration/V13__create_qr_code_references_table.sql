CREATE TABLE IF NOT EXISTS qr_code_references (
    id SERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    content VARCHAR(100) NOT NULL,
    status VARCHAR(10) NOT NULL
);
