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
                if (StringUtils.startsWith(strings[0], ".")) {
                    strings[0] = "0" + strings[0];
                }
                amount = new BigDecimal(strings[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new TipBotErrorException(DefaultMessage.INVALID_AMOUNT);
            }

            ResponseWrapperDto<WalletAccountDto> fullWalletResponse = userService.getFullWalletAccountByUserId(message.getFrom().getId());
            if (fullWalletResponse.hasErrors()) {
                logger.error(fullWalletResponse.getErrorMessage());
                throw new TipBotErrorException(fullWalletResponse.getErrorMessage());
            }

            WalletAccountDto senderWallet = new WalletAccountDto(fullWalletResponse.getResponse().getPrivateKey(), fullWalletResponse.getResponse().getReceiverAddress());
            WalletAccountDto receiverWallet = new WalletAccountDto(null, developerWallet);

            ResponseWrapperDto<TransactionResponseDto> sendResponse = blockchainGateway.sendFunds(new TransferRequestDto(senderWallet, receiverWallet, amount));

            if (sendResponse.hasErrors() || sendResponse.getResponse() == null || StringUtils.isBlank(sendResponse.getResponse().getTransactionHash())) {
                throw new TipBotErrorException(sendResponse.getErrorMessage());
            } else {
                String transactionHash = sendResponse.getResponse().getTransactionHash();

                if (!message.getChat().isUserChat()) {
                    String messageString = "Thank you very much for supporting this project! <3";
                    messageObject.enableMarkdown(true);
                    messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV1(messageString, true));
                    try {
                        absSender.execute(messageObject);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    SendMessage privateChatMessage = new SendMessage();
                    privateChatMessage.setChatId(message.getFrom().getId() + "");
                    privateChatMessage.enableMarkdown(true);
                    String privateMessageString = "You have donated " + amount + " $MTV to the developer wallet. \nThank your very much for supporting this project!!!\n\nThis is your transaction hash:\n[" + transactionHash + "](https://e.mtv.ac/transaction.html?hash=" + transactionHash + ")";
                    privateChatMessage.setText(MessageFormatHelper.escapeStringMarkdownV1(privateMessageString));
                    absSender.execute(privateChatMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
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
