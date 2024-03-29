package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.EncryptionPairDto;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.CryptoHelper;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.helper.DefaultMessage;
import ac.asimov.mtvtipbot.model.User;
import ac.asimov.mtvtipbot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class RegisterCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    @Override
    public String getCommandIdentifier() {
        return "register";
    }

    @Override
    public String getDescription() {
        return "Setup a wallet for your Telegram account";
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
            if (!message.getChat().isUserChat()) {
                throw new TipBotErrorException(DefaultMessage.PRIVATE_CHAT_COMMAND_IN_PUBLIC_CHAT);
            }

            if (userService.doesUserIdExist(message.getFrom().getId())) {
                throw new TipBotErrorException("You already have an account");
            }

            try {
                WalletAccountDto wallet = blockchainGateway.generateNewWallet();
                String userKey = CryptoHelper.userIdToKey(message.getFrom().getId());
                EncryptionPairDto encryptedPrivateKey = CryptoHelper.encrypt(wallet.getPrivateKey(), Long.toString(message.getFrom().getId()));

                ResponseWrapperDto<User> createUserResponse = userService.createUser(
                        userKey,
                        message.getFrom().getUserName(),
                        encryptedPrivateKey.getEncryptedValue(),
                        encryptedPrivateKey.getSalt(),
                        wallet.getReceiverAddress());
                if (createUserResponse.hasErrors()) {
                    logger.error(createUserResponse.getErrorMessage());
                    throw new TipBotErrorException("Error during account loading. Please notify developer");
                } else {
                    String messageString = "Success: Here is your private key: \n\n"
                            + wallet.getPrivateKey() + "\n\n"
                            + "Keep it secure. When you lose it you cannot recover your funds!!!\n"
                            + "This bot is developed and maintained by https://asimov.ac and not associated with the official MultiVAC.\n"
                            + "Please notice your tipbot wallet is not as secure as your private wallet and you should not hold large amounts of funds in it.";
                    messageObject.enableMarkdown(true);
                    messageObject.setText(MessageFormatHelper.escapeStringMarkdownV1(messageString));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new TipBotErrorException("Cannot create new wallet. Please notify the developer");
            }
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
