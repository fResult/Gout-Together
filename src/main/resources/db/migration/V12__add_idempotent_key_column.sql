ALTER TABLE bookings
ADD COLUMN idempotent_key VARCHAR(40) NOT NULL;

ALTER TABLE transactions
ADD COLUMN idempotent_key VARCHAR(40) NOT NULL;
