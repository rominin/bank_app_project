CREATE TABLE IF NOT EXISTS accounts_schema.user_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES accounts_schema.users(id) ON DELETE CASCADE,
    currency VARCHAR(10) NOT NULL,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0,
    UNIQUE (user_id, currency)
);