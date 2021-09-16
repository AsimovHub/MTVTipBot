package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.EncryptionPairDto;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.helper.CryptoHelper;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.model.User;
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

import javax.annotation.meta.When;
import java.text.MessageFormat;

public class RegisterCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    private final String commandIdentifier;
    private final String commandDescription;

    public RegisterCommand(String commandIdentifier, String commandDescription) {
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


        if (message.getChat().isUserChat()) {
            String username = null;
            try {
                String messageString;
                Long userId = absSender.getMe().getId();

                try {
                    username = absSender.getMe().getUserName();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                if (userService.doesUserIdExist(userId)) {
                    messageObject.setText(MessageFormatHelper.escapeString("You already have an account"));
                } else {
                    try {
                        WalletAccountDto wallet = blockchainGateway.generateNewWallet();
                        String userKey = CryptoHelper.userIdToKey(userId);
                        if (!StringUtils.equals("1234", Long.toString(1234L))) {
                            throw new Exception("Rework id comparison below");
                        }
                        EncryptionPairDto encryptedPrivateKey = CryptoHelper.encrypt(wallet.getPrivateKey(), Long.toString(userId));

                        ResponseWrapperDto<User> createUserResponse = userService.createUser(userKey, username, encryptedPrivateKey.getEncryptedValue(), encryptedPrivateKey.getSalt(), wallet.getReceiverAddress());
                        if (createUserResponse.hasErrors()) {
                            logger.error(createUserResponse.getErrorMessage());
                            messageString = "Error during account initialization. Please notify developer";
                        } else {
                            messageString = "Success: Here is your private key. Keep it secure.\n"
                                    + "When you lose it you cannot recover your funds!!! \n\n"
                                    + wallet.getPrivateKey();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        messageString = "Cannot create new wallet. Please notify the developer";
                    }
                    messageObject.setText(MessageFormatHelper.appendDisclaimer(messageString, true));
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
                messageObject.setText(MessageFormatHelper.escapeString("Cannot determine userdata"));
            }
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
