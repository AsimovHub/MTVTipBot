package ac.asimov.mtvtipbot;

import ac.asimov.mtvtipbot.dao.TransactionDao;
import ac.asimov.mtvtipbot.dao.UserDao;
import ac.asimov.mtvtipbot.model.Transaction;
import ac.asimov.mtvtipbot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;

@Service
public class MigrationService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private TransactionDao transactionDao;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateUsers() {
        Connection conn = null;
        try {
            File file = new File(getClass().getClassLoader().getResource("database/database.db").getFile());
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());



            String sql = "SELECT userId, username, publicKey, privateKey, salt, createdAt FROM users";

            /*
            user_key	VARCHAR(255) NOT NULL,
                         username VARCHAR(255) NOT NULL,
                         public_key VARCHAR(255) NOT NULL,
                         private_key VARCHAR(255) NOT NULL,
                         created_at DATETIME NOT NULL,
                         salt VARCHAR(255) NOT NULL,
             */

             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                try {
                    String userKey = rs.getString("userId");
                    if (userDao.findByUserKey(userKey).isPresent()) {
                        continue;
                    }
                    String username = rs.getString("username");
                    String publicKey = rs.getString("publicKey");
                    String privateKey = rs.getString("privateKey");
                    String salt = rs.getString("salt");
                    Timestamp timestamp = rs.getTimestamp("createdAt");
                    User user = new User();
                    user.setUserKey(userKey);
                    user.setUsername(username);
                    user.setPublicKey(publicKey);
                    user.setPrivateKey(privateKey);
                    user.setSalt(salt);
                    user.setCreatedAt(timestamp.toLocalDateTime());
                    userDao.save(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateTransactions() {
        Connection conn = null;
        try {
            File file = new File(getClass().getClassLoader().getResource("database/database.db").getFile());
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

            String sql = "SELECT transactionHash, transferredAt, senderWallet, receiverWallet, amount FROM transactions";

            /*
            user_key	VARCHAR(255) NOT NULL,
                         username VARCHAR(255) NOT NULL,
                         public_key VARCHAR(255) NOT NULL,
                         private_key VARCHAR(255) NOT NULL,
                         created_at DATETIME NOT NULL,
                         salt VARCHAR(255) NOT NULL,
             */

            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                String transactionHash = rs.getString("transactionHash");
                if (transactionDao.findByTransactionHash(transactionHash).isPresent()) {
                    continue;
                }
                String senderWallet = rs.getString("senderWallet");
                String receiverWallet = rs.getString("receiverWallet");
                BigDecimal amount = rs.getBigDecimal("amount");
                Timestamp timestamp = rs.getTimestamp("transferredAt");
                Transaction transaction = new Transaction();
                transaction.setTransactionHash(transactionHash);
                transaction.setSenderWallet(senderWallet);
                transaction.setReceiverWallet(receiverWallet);
                transaction.setAmount(amount);
                transaction.setTransferredAt(timestamp.toLocalDateTime());
                transactionDao.save(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
