CREATE TABLE IF NOT EXISTS users (
                         userId	VARCHAR,
                         username	TEXT,
                         publicKey	TEXT,
                         privateKey	TEXT,
                         createdAt	INTEGER,
                         salt	TEXT,
                         PRIMARY KEY(userId)
);