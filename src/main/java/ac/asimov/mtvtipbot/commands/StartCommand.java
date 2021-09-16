package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String commandIdentifier;
    private final String commandDescription;

    public StartCommand(String commandIdentifier, String commandDescription) {
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


        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the [MultiVAC Tip Bot](https://asimov.ac/tipbot)\n");
        sb.append("This is brought to you by n\\-three as one of the first projects using the MultiVAC blockchain and is not associated with the official MultiVAC team.\n");
        sb.append("You can find all of my projects around the MultiVAC blockchain on the [Asimov Hub](https://asimov.ac)\n");
        sb.append("\n");
        sb.append("Please notice your tipbot wallet is not as secure as your private wallet and you should not hold large amounts of funds in it.");

        sb.append("Welcome to the [MultiVAC Tip Bot](https://asimov.ac/tipbot)\n\n");
        sb.append("This is brought to you by n\\-three as one of the first projects using the MultiVAC blockchain and is not associated with the official MultiVAC team.\n");

        sb.append("You can find all of my projects around the MultiVAC blockchain on the Asimov Hub (https://asimov.ac)\n");
        sb.append("Use /help to get an overview of commands and /register to create a tipbot managed wallet.\n");
        sb.append("If you like this bot and want to support development of this bot or the Asimov Hub you can donate me some MTV by using /donate [amount]\n");
        sb.append("You can find all of my projects around the MultiVAC blockchain on the Asimov Hub (https://asimov.ac)\n");
        sb.append("Please notice your tipbot wallet is not as secure as your private wallet and you should not hold large amounts of funds in it.");

        messageObject.setText(MessageFormatHelper.escapeString(sb.toString()));
        try {
            absSender.execute(messageObject);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
