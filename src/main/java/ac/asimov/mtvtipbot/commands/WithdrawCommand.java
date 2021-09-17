package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.TransactionResponseDto;
import ac.asimov.mtvtipbot.dtos.TransferRequestDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.web3j.crypto.Wallet;

import java.math.BigDecimal;

public class WithdrawCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    @Autowired
    private UserService userService;

    private final String commandIdentifier;
    private final String commandDescription;

    public WithdrawCommand(String commandIdentifier, String commandDescription) {
        this.commandIdentifier = commandIdentifier;
        this.commandDescription = commandDescription;
    }

    @Override
    public String getCommandIdentifier() {
        return commandIdentifier;
    }

    @Override
    public String getDescription() {
        return commandDescription;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {

        SendMessage messageObject = new SendMessage();

        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());
        messageObject.enableMarkdownV2(true);

        String messageString;

        if (message.getChat().isUserChat()) {
            if (strings.length == 0) {
                // Invalid usage
                messageString = "Invalid usage! Please use /withdraw [wallet] or /withdraw [wallet] [amount]";
            } else if (strings.length == 1) {
                // Withdraw complete
                String walletString = strings[0];
                if (blockchainGateway.isWalletValid(new WalletAccountDto(null, walletString))) {
                    ResponseWrapperDto<WalletAccountDto> fullWalletResponse = userService.getFullWalletAccountByUserId(message.getFrom().getId());
                    if (!fullWalletResponse.hasErrors()) {
                        WalletAccountDto senderWallet = new WalletAccountDto(fullWalletResponse.getResponse().getPrivateKey(), fullWalletResponse.getResponse().getReceiverAddress());
                        WalletAccountDto receiverWallet = new WalletAccountDto(null, walletString);
                        ResponseWrapperDto<TransactionResponseDto> sendResponse = blockchainGateway.sendCompleteFunds(new TransferRequestDto(senderWallet, receiverWallet));
                        if (sendResponse.hasErrors() || sendResponse.getResponse() == null || StringUtils.isBlank(sendResponse.getResponse().getTransactionHash())) {
                            messageString = "An error occurred during transaction. Please try again later and notify developer, when this problem remains";
                        } else {
                            String transactionHash = sendResponse.getResponse().getTransactionHash();
                            messageString = "You successfully sent all your $MTV to your wallet.\nThis is the transaction hash:\n[" + transactionHash + "](https://e.mtv.ac/transaction.html?hash=" + transactionHash + ")";
                        }
                    } else {
                        logger.error(fullWalletResponse.getErrorMessage());
                        messageString = "Cannot withdraw funds. Some error happend while loading your data";
                    }
                } else {
                    messageString = "Invalid address!";
                }
            } else if (strings.length == 2) {
                // Withdraw given amount
                try {
                    String walletString = strings[0];
                    BigDecimal amount = new BigDecimal(strings[1]);
                    if (blockchainGateway.isWalletValid(new WalletAccountDto(null, walletString))) {
                        ResponseWrapperDto<WalletAccountDto> fullWalletResponse = userService.getFullWalletAccountByUserId(message.getFrom().getId());
                        if (!fullWalletResponse.hasErrors()) {
                            WalletAccountDto senderWallet = new WalletAccountDto(fullWalletResponse.getResponse().getPrivateKey(), fullWalletResponse.getResponse().getReceiverAddress());
                            WalletAccountDto receiverWallet = new WalletAccountDto(null, walletString);
                            ResponseWrapperDto<TransactionResponseDto> sendResponse = blockchainGateway.sendCompleteFunds(new TransferRequestDto(senderWallet, receiverWallet, amount));
                            if (sendResponse.hasErrors() || sendResponse.getResponse() == null || StringUtils.isBlank(sendResponse.getResponse().getTransactionHash())) {
                                messageString = "An error occurred during transaction. Please try again later and notify developer, when this problem remains";
                            } else {
                                String transactionHash = sendResponse.getResponse().getTransactionHash();
                                messageString = "You successfully sent " + amount + " $MTV to your wallet.\nThis is the transaction hash:\n[" + transactionHash + "](https://e.mtv.ac/transaction.html?hash=" + transactionHash + ")";
                            }
                        } else {
                            logger.error(fullWalletResponse.getErrorMessage());
                            messageString = "Cannot withdraw funds. Some error happend while loading your data";
                        }
                    } else {
                        messageString = "Invalid address!";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    messageString = "Invalid amount!";
                }
            } else {
                // Invalid usage
                messageString = "Invalid usage! Please use /withdraw [wallet] or /withdraw [wallet] [amount]";
            }
            messageObject.setText(MessageFormatHelper.escapeString(messageString));
        } else {
            messageObject.setText(MessageFormatHelper.escapeString("This command can only be used in private chat. Send me a message!"));
        }

        try {
            absSender.execute(messageObject);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
