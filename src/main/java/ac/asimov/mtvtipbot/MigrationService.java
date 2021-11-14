package ac.asimov.mtvtipbot;

import ac.asimov.mtvtipbot.dao.TransactionDao;
import ac.asimov.mtvtipbot.dao.UserDao;
import ac.asimov.mtvtipbot.model.Transaction;
import ac.asimov.mtvtipbot.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;

/**
 * This service is used to migrate the sqlite database of the original tipbot (written using nodejs) to the current tipbot's mysql database
 */

@Service
public class MigrationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    @Autowired
    private TransactionDao transactionDao;

    @Value("${tipbot.database.migrationFolder}")
    private String databasePath;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateUsers() {
        logger.info("Scanning for database to migrate users");
        Connection conn = null;
        try {
            File file = new File(databasePath);
            if (!file.exists()) {
                logger.info("No database for user migration found");
                return;
            }
            logger.info("Database for user migration found");
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            String sql = "SELECT userId, username, publicKey, privateKey, salt, createdAt FROM users";

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
            logger.info("User database successfully migrated");
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
        logger.info("Scanning for database to migrate transaction");
        Connection conn = null;
        try {
            File file = new File(databasePath);
            if (!file.exists()) {
                logger.info("No database for user migration found");
                return;
            }
            logger.info("Database for user migration found");

            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            String sql = "SELECT transactionHash, transferredAt, senderWallet, receiverWallet, amount FROM transactions";

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
            logger.info("Transaction database successfully migrated");

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
