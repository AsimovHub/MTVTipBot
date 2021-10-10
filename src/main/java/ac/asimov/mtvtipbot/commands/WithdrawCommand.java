package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.TransactionResponseDto;
import ac.asimov.mtvtipbot.dtos.TransferRequestDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.helper.DefaultMessage;
import ac.asimov.mtvtipbot.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

@Component
public class WithdrawCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    @Autowired
    private UserService userService;

    @Autowired
    public WithdrawCommand() {
    }

    @Override
    public String getCommandIdentifier() {
        return "withdraw";
    }

    @Override
    public String getDescription() {
        return "Withdraw given amount of MTV to your given wallet";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        if (message.getFrom().getIsBot()) {
            return;
        }
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());

        try {
            String messageString;

            if (!message.getChat().isUserChat()) {
                throw new TipBotErrorException(DefaultMessage.PRIVATE_CHAT_COMMAND_IN_PUBLIC_CHAT);
            }

            ResponseWrapperDto<WalletAccountDto> fullWalletResponse = userService.getFullWalletAccountByUserId(message.getFrom().getId());
            if (fullWalletResponse.hasErrors()) {
                logger.error(fullWalletResponse.getErrorMessage());
                throw new TipBotErrorException(fullWalletResponse.getErrorMessage());
            }

            if (strings.length == 1) {
                // Withdraw complete
                String walletString = strings[0];
                if (blockchainGateway.isWalletValid(new WalletAccountDto(null, walletString))) {

                    WalletAccountDto senderWallet = new WalletAccountDto(fullWalletResponse.getResponse().getPrivateKey(), fullWalletResponse.getResponse().getReceiverAddress());
                    WalletAccountDto receiverWallet = new WalletAccountDto(null, walletString);
                    ResponseWrapperDto<TransactionResponseDto> sendResponse = blockchainGateway.sendCompleteFunds(new TransferRequestDto(senderWallet, receiverWallet));
                    if (sendResponse.hasErrors() || sendResponse.getResponse() == null || StringUtils.isBlank(sendResponse.getResponse().getTransactionHash())) {
                        throw new TipBotErrorException(sendResponse.getErrorMessage());
                    } else {
                        String transactionHash = sendResponse.getResponse().getTransactionHash();
                        messageString = "You successfully sent all your $MTV to your wallet.\n\nThis is your transaction hash:\n[" + transactionHash + "](https://e.mtv.ac/transaction.html?hash=" + transactionHash + ")";
                    }
                } else {
                    throw new TipBotErrorException("Invalid address!");
                }
            } else if (strings.length == 2) {
                // Withdraw given amount
                try {
                    String walletString = strings[0];
                    if (StringUtils.startsWith(strings[1], ".")) {
                        strings[1] = "0" + strings[1];
                    }
                    BigDecimal amount = new BigDecimal(strings[1]);
                    if (blockchainGateway.isWalletValid(new WalletAccountDto(null, walletString))) {
                        WalletAccountDto senderWallet = new WalletAccountDto(fullWalletResponse.getResponse().getPrivateKey(), fullWalletResponse.getResponse().getReceiverAddress());
                        WalletAccountDto receiverWallet = new WalletAccountDto(null, walletString);
                        ResponseWrapperDto<TransactionResponseDto> sendResponse = blockchainGateway.sendFunds(new TransferRequestDto(senderWallet, receiverWallet, amount));
                        if (sendResponse.hasErrors() || sendResponse.getResponse() == null || StringUtils.isBlank(sendResponse.getResponse().getTransactionHash())) {
                            throw new TipBotErrorException(sendResponse.getErrorMessage());
                        } else {
                            String transactionHash = sendResponse.getResponse().getTransactionHash();
                            messageString = "You successfully sent " + amount + " $MTV to your wallet.\n\nThis is the your hash:\n[" + transactionHash + "](https://e.mtv.ac/transaction.html?hash=" + transactionHash + ")";
                        }
                    } else {
                        throw new TipBotErrorException("Invalid address!");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new TipBotErrorException(DefaultMessage.INVALID_AMOUNT);
                }
            } else {
                throw new TipBotErrorException("Invalid usage! Please use /withdraw [wallet] or /withdraw [wallet] [amount]");
            }

            messageObject.enableMarkdown(true);
            messageObject.setText(MessageFormatHelper.escapeStringMarkdownV1(messageString));
            try {
                absSender.execute(messageObject);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                throw new TipBotErrorException(DefaultMessage.SERVER_ERROR);
            }
        } catch (TipBotErrorException e) {
            e.printStackTrace();
            messageObject.enableMarkdownV2(true);
            messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV2(e.getMessage(), true));
            try {
                absSender.execute(messageObject);
            } catch (TelegramApiException e1) {
                e1.printStackTrace();
            }
        }
    }
}
