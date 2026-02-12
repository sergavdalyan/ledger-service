CREATE TABLE accounts (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    type       VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
    id          BIGSERIAL    PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    date        TIMESTAMP    NOT NULL DEFAULT now(),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE transaction_entries (
    id             BIGSERIAL      PRIMARY KEY,
    transaction_id BIGINT         NOT NULL REFERENCES transactions(id),
    account_id     BIGINT         NOT NULL REFERENCES accounts(id),
    entry_type     VARCHAR(10)    NOT NULL,
    amount         NUMERIC(19, 4) NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_entries_transaction_id ON transaction_entries(transaction_id);
CREATE INDEX idx_entries_account_id ON transaction_entries(account_id);
CREATE INDEX idx_entries_account_entry_type ON transaction_entries(account_id, entry_type);
