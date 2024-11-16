ALTER TABLE tour_company_wallets
ALTER COLUMN last_updated TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE user_points
ALTER COLUMN last_updated TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE user_wallets
ALTER COLUMN last_updated TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE bookings
ALTER COLUMN booking_date TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE bookings
ALTER COLUMN last_updated TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE transactions
ALTER COLUMN transaction_date TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE tours
ALTER COLUMN activity_date TYPE TIMESTAMP WITH TIME ZONE;
