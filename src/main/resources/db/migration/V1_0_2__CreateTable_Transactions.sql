CREATE TABLE IF NOT EXISTS transactions (
                                transactionHash	TEXT,
                                transferredAt	INTEGER,
                                senderWallet	TEXT,
                                receiverWallet	TEXT,
                                amount	INTEGER,
                                PRIMARY KEY(transactionHash)
);