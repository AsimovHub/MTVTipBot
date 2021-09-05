CREATE TABLE IF NOT EXISTS transactions (
                                transaction_hash	VARCHAR(255) NOT NULL,
                                transferred_at DATETIME NOT NULL,
                                sender_wallet VARCHAR(255) NOT NULL,
                                receiver_wallet VARCHAR(255) NOT NULL,
                                amount BIGINT NOT NULL,
                                PRIMARY KEY(transaction_hash)
);