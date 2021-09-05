package ac.asimov.mtvtipbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BalanceCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String commandIdentifier;
    private final String commandDescription;

    public BalanceCommand(String commandIdentifier, String commandDescription) {
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
        if (message.getChat().isUserChat()) {
            // DO YOUR STUFF
        } else {
            SendMessage helpMessage = new SendMessage();
            helpMessage.setChatId(message.getChatId().toString());
            helpMessage.enableHtml(true);
            helpMessage.setText("This command can only be used in private chat. Send me a message!");
        }
        SendMessage helpMessage = new SendMessage();
        helpMessage.setChatId(message.getChatId().toString());
        helpMessage.enableHtml(true);
        helpMessage.setText("Java Telegram Bot Test1 23: " + getCommandIdentifier());

        try {
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
