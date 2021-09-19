package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.*;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.model.User;
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
public class MTVSendCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    public MTVSendCommand() {
    }

    @Override
    public String getCommandIdentifier() {
        return "mtvsend";
    }

    @Override
    public String getDescription() {
        return "Send MTV to the given user";
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

            if (strings.length != 2) {
                throw new TipBotErrorException("Invalid usage! Please reply /mtvsend [@user] [amount] or /mtvsend [wallet] [amount]");
            }

            // Withdraw given amount
            try {
                WalletAccountDto receiverWallet;
                if (StringUtils.startsWith(strings[0], "@")) {
                    ResponseWrapperDto<User> userResponse = userService.getUserByUsername(strings[0].substring(1));
                    if (userResponse.hasErrors()) {
                        throw new TipBotErrorException(userResponse.getErrorMessage());
                    }
                    if (userResponse.getResponse() != null) {
                        receiverWallet = new WalletAccountDto(null, userResponse.getResponse().getPublicKey());
                    } else {
                        throw new TipBotErrorException("User does not have tipbot account");
                    }
                } else if (blockchainGateway.isWalletValid(new WalletAccountDto(null, strings[0]))) {
                    receiverWallet = new WalletAccountDto(null, strings[0]);
                } else {
                    throw new TipBotErrorException("You must mention a user or use a valid wallet address (this starts with 0x)\n/mtvsend [user/wallet] [amount]");
                }

                if (StringUtils.startsWith(strings[1], ".")) {
                    strings[1] = "0" + strings[1];
                }
                BigDecimal amount = new BigDecimal(strings[1]);
                Long senderUserId =  message.getFrom().getId();

                if (!userService.doesUserIdExist(senderUserId)) {
                    throw new TipBotErrorException("You do not have an account yet.\nOpen up private chat to create an account.");
                }

                ResponseWrapperDto<WalletAccountDto> senderWalletResponse = userService.getFullWalletAccountByUserId(senderUserId);
                if (senderWalletResponse.hasErrors()) {
                    throw new TipBotErrorException(senderWalletResponse.getErrorMessage());
                }

                try {
                    ResponseWrapperDto<TransactionResponseDto> transferResponse = blockchainGateway.sendFunds(new TransferRequestDto(senderWalletResponse.getResponse(), receiverWallet, amount));
                    if (transferResponse.hasErrors()) {
                        throw new TipBotErrorException("Cannot transfer funds");
                    } else {
                        String messageString = "You successfully sent " + amount + " $MTV to " + strings[0];
                        messageObject.enableMarkdown(true);
                        messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV1(messageString, true));
                        try {
                            absSender.execute(messageObject);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            throw new TipBotErrorException("Server error");
                        }

                        if (!message.getChat().isUserChat()) {
                            try {
                                SendMessage privateChatMessage = new SendMessage();
                                privateChatMessage.setChatId(message.getFrom().getId() + "");
                                privateChatMessage.enableMarkdown(true);
                                String privateMessageString = "You have sent " + amount + " $MTV to " + strings[0] + "\nThis is your transaction hash:\n[" + transferResponse.getResponse().getTransactionHash() + "](https://e.mtv.ac/transaction.html?hash=" + transferResponse.getResponse().getTransactionHash() + ")";
                                privateChatMessage.setText(MessageFormatHelper.escapeStringMarkdownV1(privateMessageString));
                                absSender.execute(privateChatMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new TipBotErrorException("cannot transfer funds");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new TipBotErrorException("Invalid amount!");
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
