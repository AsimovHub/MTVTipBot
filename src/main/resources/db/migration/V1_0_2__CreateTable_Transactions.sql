CREATE TABLE IF NOT EXISTS transactions (
                                transaction_hash	VARCHAR(255) NOT NULL,
                                transferred_at DATETIME NOT NULL,
                                sender_wallet VARCHAR(255) NOT NULL,
                                receiver_wallet VARCHAR(255) NOT NULL,
                                amount DECIMAL(19,2) NOT NULL,
                                PRIMARY KEY(transaction_hash)
);