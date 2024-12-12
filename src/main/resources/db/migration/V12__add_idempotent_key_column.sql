ALTER TABLE bookings
ADD COLUMN idempotent_key TYPE VARCHAR(40);

ALTER TABLE transactions
ADD COLUMN idempotent_key TYPE VARCHAR(40);
