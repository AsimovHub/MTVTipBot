CREATE TABLE IF NOT EXISTS users (
                         user_key	VARCHAR(255) NOT NULL,
                         username VARCHAR(255) NOT NULL,
                         public_key VARCHAR(255) NOT NULL,
                         private_key VARCHAR(255) NOT NULL,
                         created_at DATETIME NOT NULL,
                         salt VARCHAR(255) NOT NULL,
                         PRIMARY KEY(user_key)
);