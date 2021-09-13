package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HelpCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String commandIdentifier;
    private final String commandDescription;

    public HelpCommand(String commandIdentifier, String commandDescription) {
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
        try {
            SendMessage helpMessage = new SendMessage();
            helpMessage.setChatId(message.getChatId().toString());
            helpMessage.setReplyToMessageId(message.getMessageId());
            helpMessage.enableMarkdownV2(true);
            String escaped = MessageFormatHelper.escapeString("^[](){}´`+*#'.:?,;!s Bot-_.Test1/23: " + getCommandIdentifier());
            helpMessage.setText(escaped);
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        try {
            SendMessage helpMessage = new SendMessage();
            helpMessage.setChatId(message.getChatId().toString());
            helpMessage.enableMarkdownV2(true);
            helpMessage.setText(MessageFormatHelper.escapeString("Private Message " + getCommandIdentifier()));
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
