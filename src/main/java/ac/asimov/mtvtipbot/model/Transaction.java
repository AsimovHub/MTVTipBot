package ac.asimov.mtvtipbot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String transactionHash;

    private LocalDateTime transferredAt;

    private String senderWallet;
    private String receiverWallet;

    private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }

    public String getSenderWallet() {
        return senderWallet;
    }

    public void setSenderWallet(String senderWallet) {
        this.senderWallet = senderWallet;
    }

    public String getReceiverWallet() {
        return receiverWallet;
    }

    public void setReceiverWallet(String receiverWallet) {
        this.receiverWallet = receiverWallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
