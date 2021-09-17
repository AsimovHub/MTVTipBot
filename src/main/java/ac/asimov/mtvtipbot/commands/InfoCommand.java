package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class InfoCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String commandIdentifier;
    private final String commandDescription;

    public InfoCommand(String commandIdentifier, String commandDescription) {
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
        if (message.getFrom().getIsBot()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the [MultiVAC Tip Bot](https://asimov.ac/tipbot)\n");
                sb.append("This is brought to you by n\\-three as one of the first projects using the MultiVAC blockchain and is not associated with the official MultiVAC team.\n");
                sb.append("You can find all of my projects around the MultiVAC blockchain on the [Asimov Hub](https://asimov.ac)\n");
                sb.append("\n");
                sb.append("Please notice your tipbot wallet is not as secure as your private wallet and you should not hold large amounts of funds in it.");


        String infoString = MessageFormatHelper.escapeString(sb.toString());

        SendMessage infoMessage = new SendMessage();
        infoMessage.setChatId(message.getChatId().toString());
        infoMessage.setReplyToMessageId(message.getMessageId());
        infoMessage.enableMarkdownV2(true);
        infoMessage.setText(infoString);

        try {
            absSender.execute(infoMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
