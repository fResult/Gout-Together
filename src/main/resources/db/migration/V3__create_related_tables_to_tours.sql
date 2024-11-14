CREATE TABLE IF NOT EXISTS tour_companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS tour_company_wallets (
    id SERIAL PRIMARY KEY,
    tour_company_id INTEGER UNIQUE NOT NULL REFERENCES tour_companies(id),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    balance DECIMAL(13, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS tour_company_logins (
    id SERIAL PRIMARY KEY,
    tour_company_id INTEGER UNIQUE NOT NULL REFERENCES tour_companies(id),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tours (
    id SERIAL PRIMARY KEY,
    tour_company_id INTEGER NOT NULL REFERENCES tour_companies(id),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    location VARCHAR(1000) NOT NULL,
    number_of_people INTEGER NOT NULL,
    activity_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS tour_counts (
    id SERIAL PRIMARY KEY,
    tour_id INTEGER UNIQUE NOT NULL REFERENCES tours(id),
    amount INTEGER NOT NULL
);
