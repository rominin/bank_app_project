CREATE SCHEMA IF NOT EXISTS exchange_schema;

CREATE TABLE IF NOT EXISTS exchange_schema.exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    rate NUMERIC(20, 10) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);