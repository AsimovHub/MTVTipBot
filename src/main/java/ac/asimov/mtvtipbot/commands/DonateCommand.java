package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.TransactionResponseDto;
import ac.asimov.mtvtipbot.dtos.TransferRequestDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

@Component
public class DonateCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${tipbot.developer-wallet}")
    private String developerWallet;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    @Autowired
    private UserService userService;

    @Override
    public String getCommandIdentifier() {
        return "donate";
    }

    @Override
    public String getDescription() {
        return "Donate MTV to the developer wallet";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {

        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());
        try {
            if (strings.length != 1) {
                throw new TipBotErrorException("Invalid usage! Please use /donate [amount]");
            }

            BigDecimal amount = null;
            try {
                amount = new BigDecimal(strings[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new TipBotErrorException("Invalid amount!");
            }
            ResponseWrapperDto<WalletAccountDto> fullWalletResponse = userService.getFullWalletAccountByUserId(message.getFrom().getId());

            if (fullWalletResponse.hasErrors()) {
                logger.error(fullWalletResponse.getErrorMessage());
                throw new TipBotErrorException("Cannot send funds. Some error happened while loading your data");
            }
            WalletAccountDto senderWallet = new WalletAccountDto(fullWalletResponse.getResponse().getPrivateKey(), fullWalletResponse.getResponse().getReceiverAddress());
            WalletAccountDto receiverWallet = new WalletAccountDto(null, developerWallet);
            ResponseWrapperDto<TransactionResponseDto> sendResponse = blockchainGateway.sendFunds(new TransferRequestDto(senderWallet, receiverWallet, amount));
            if (sendResponse.hasErrors() || sendResponse.getResponse() == null || StringUtils.isBlank(sendResponse.getResponse().getTransactionHash())) {
                throw new TipBotErrorException(sendResponse.getErrorMessage());
            } else {
                String transactionHash = sendResponse.getResponse().getTransactionHash();
                String messageString = "You successfully sent " + amount + " $MTV to your wallet.\nThis is the transaction hash:\n[" + transactionHash + "](https://e.mtv.ac/transaction.html?hash=" + transactionHash + ")";
                messageObject.enableMarkdown(true);
                messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV1(messageString, true));
                try {
                    absSender.execute(messageObject);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                if (!message.getChat().isUserChat()) {
                    // TODO: Send another message in private chat when this is in group chat

                    SendMessage privateChatMessage = new SendMessage();
                    privateChatMessage.setChatId(message.getFrom().getId() + "");
                    privateChatMessage.enableMarkdown(true);

                    String privateMessageString = "";
                    // TODO: Add message


                    privateChatMessage.setText(MessageFormatHelper.escapeStringMarkdownV1(privateMessageString));

                    try {
                        absSender.execute(privateChatMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
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
