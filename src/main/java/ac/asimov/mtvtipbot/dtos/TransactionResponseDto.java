package ac.asimov.mtvtipbot.dtos;

public class TransactionResponseDto {

    private String transactionHash;

    public TransactionResponseDto(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
