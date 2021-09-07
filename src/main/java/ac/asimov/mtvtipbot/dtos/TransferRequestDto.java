package ac.asimov.mtvtipbot.dtos;


import java.math.BigDecimal;

public class TransferRequestDto {

    private WalletAccountDto sender;

    private WalletAccountDto receiver;

    private BigDecimal amount;

    public TransferRequestDto(WalletAccountDto sender, WalletAccountDto receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public TransferRequestDto(WalletAccountDto sender, WalletAccountDto receiver, BigDecimal amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public WalletAccountDto getSender() {
        return sender;
    }

    public void setSender(WalletAccountDto sender) {
        this.sender = sender;
    }

    public WalletAccountDto getReceiver() {
        return receiver;
    }

    public void setReceiver(WalletAccountDto receiver) {
        this.receiver = receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
