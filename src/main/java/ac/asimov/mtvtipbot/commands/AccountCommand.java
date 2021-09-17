package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.model.User;
import ac.asimov.mtvtipbot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AccountCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    private final String commandIdentifier;
    private final String commandDescription;

    public AccountCommand(String commandIdentifier, String commandDescription) {
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
            // DO YOUR STUFF

            String messageString;
            Long userId = message.getFrom().getId();
            ResponseWrapperDto<User> userResponse = userService.getUserByUserId(userId);
            if (userResponse.hasErrors()) {
                messageString = "Error during account initialization. Please notify developer";
            } else {
                if (userResponse.getResponse() == null) {
                    messageString = "You do not have an account yet. Please use /register";
                } else {
                    messageString = "Your wallet address is: [" + userResponse.getResponse().getPublicKey() + "](https://e.mtv.ac/account.html?address=" + userResponse.getResponse().getPublicKey() + ")";
                }
            }
            messageObject.setText(MessageFormatHelper.appendDisclaimer(messageString, true));
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
