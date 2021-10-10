package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.*;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.model.DefaultMessage;
import ac.asimov.mtvtipbot.model.User;
import ac.asimov.mtvtipbot.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

@Component
public class MTVTipCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private UserService userService;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    public MTVTipCommand() {
    }

    @Override
    public String getCommandIdentifier() {
        return "mtvtip";
    }

    @Override
    public String getDescription() {
        return "Send given tip amount to replied user";
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
            if (message.getChat().isUserChat()) {
                throw new TipBotErrorException("This command can only be used in public chats (since you need to reply to another user).");
            }

            if (strings.length != 1) {
                throw new TipBotErrorException("Invalid usage! Please reply /mtvtip [amount] to a sent message");
            }

            if (!message.isReply() || message.getReplyToMessage() == null || message.getReplyToMessage().getFrom() == null) {
                throw new TipBotErrorException("This command can only be used as reply to another message");
            }

            if (message.getReplyToMessage().getFrom().getIsBot()) {
                throw new TipBotErrorException("You can send no MTV to bots");
            }

            // Withdraw given amount
            try {
                if (StringUtils.startsWith(strings[0], ".")) {
                    strings[0] = "0" + strings[0];
                }
                BigDecimal amount = new BigDecimal(strings[0]);
                Long senderUserId =  message.getFrom().getId();
                Long receiverUserId = message.getReplyToMessage().getFrom().getId();

                if (!userService.doesUserIdExist(senderUserId)) {
                    throw new TipBotErrorException(DefaultMessage.NO_ACCOUNT_PLEASE_REGISTER);
                }

                ResponseWrapperDto<WalletAccountDto> receiverWalletResponse = userService.getWalletAddressByUserId(receiverUserId);

                if (receiverWalletResponse.hasErrors()) {
                    throw new TipBotErrorException(receiverWalletResponse.getErrorMessage());
                }

                ResponseWrapperDto<WalletAccountDto> senderWalletResponse = userService.getFullWalletAccountByUserId(senderUserId);
                if (senderWalletResponse.hasErrors()) {
                    throw new TipBotErrorException(senderWalletResponse.getErrorMessage());
                }

                ResponseWrapperDto<TransactionResponseDto> transferResponse = blockchainGateway.sendFunds(new TransferRequestDto(senderWalletResponse.getResponse(), receiverWalletResponse.getResponse(), amount));
                if (transferResponse.hasErrors() || transferResponse.getResponse() == null || StringUtils.isBlank(transferResponse.getResponse().getTransactionHash())) {
                    if (StringUtils.equalsIgnoreCase(transferResponse.getErrorMessage(), "already known")) {
                        throw new TipBotErrorException(DefaultMessage.UNKNOWN_ERROR_CHECK_ACCOUNT_BALANCE);
                    }
                    throw new TipBotErrorException(transferResponse.getErrorMessage());
                } else {
                    String messageString;
                    if (StringUtils.isBlank(message.getReplyToMessage().getFrom().getUserName())) {
                        messageString = "You successfully sent " + amount + " $MTV";
                    } else {
                        messageString = "You successfully sent " + amount + " $MTV to @" + message.getReplyToMessage().getFrom().getUserName();
                    }
                    messageObject.enableMarkdown(true);
                    messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV1(messageString, true));
                    try {
                        absSender.execute(messageObject);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        throw new TipBotErrorException(DefaultMessage.SERVER_ERROR);
                    }

                    try {
                        SendMessage privateChatMessage = new SendMessage();
                        privateChatMessage.setChatId(message.getFrom().getId() + "");
                        privateChatMessage.enableMarkdown(true);
                        String privateMessageString;
                        if (StringUtils.isBlank(message.getReplyToMessage().getFrom().getUserName())) {
                            privateMessageString = "You have sent " + amount + " $MTV to an user without username.\n\nThis is your transaction hash:\n[" + transferResponse.getResponse().getTransactionHash() + "](https://e.mtv.ac/transaction.html?hash=" + transferResponse.getResponse().getTransactionHash() + ")";
                        } else {
                            privateMessageString = "You have sent " + amount + " $MTV to @" + message.getReplyToMessage().getFrom().getUserName() + "\n\nThis is your transaction hash:\n[" + transferResponse.getResponse().getTransactionHash() + "](https://e.mtv.ac/transaction.html?hash=" + transferResponse.getResponse().getTransactionHash() + ")";
                        }

                        privateChatMessage.setText(MessageFormatHelper.escapeStringMarkdownV1(privateMessageString));
                        absSender.execute(privateChatMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new TipBotErrorException(DefaultMessage.INVALID_AMOUNT);
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
