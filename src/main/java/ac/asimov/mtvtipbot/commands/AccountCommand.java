package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
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
public class AccountCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Override
    public String getCommandIdentifier() {
        return "account";
    }

    @Override
    public String getDescription() {
        return "Show your account address";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());

        try {
            if (!message.getChat().isUserChat()) {
                throw new TipBotErrorException("This command can only be used in private chat. Send me a message!");

            }
            Long userId = message.getFrom().getId();
            ResponseWrapperDto<User> userResponse = userService.getUserByUserId(userId);
            if (userResponse.hasErrors()) {
                throw new TipBotErrorException("Cannot load account. Please notify developer.");
            } else {
                if (userResponse.getResponse() == null) {
                    throw new TipBotErrorException("You do not have an account yet. Please use /register");
                } else {
                    String messageString = "Your wallet address is: [" + userResponse.getResponse().getPublicKey() + "](https://e.mtv.ac/account.html?address=" + userResponse.getResponse().getPublicKey() + ")";
                    messageObject.enableMarkdown(true);
                    messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV1(messageString, true));
                }
            }
            try {
                absSender.execute(messageObject);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                throw new TipBotErrorException("Server error");
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
