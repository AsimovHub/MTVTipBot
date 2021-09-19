package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class FaucetCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getCommandIdentifier() {
        return "faucet";
    }

    @Override
    public String getDescription() {
        return "Claim some free MTV from the Asimov faucet";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        if (message.getFrom().getIsBot()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hi there, faucet claiming directly in Telegram is not supported yet but you can get your address with /account, open up the faucet by clicking [this link](https://asimov.ac/faucet) and enter your address");

        String messageString = MessageFormatHelper.escapeStringMarkdownV1(sb.toString());

        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());
        messageObject.enableMarkdown(true);
        messageObject.setText(messageString);

        try {
            absSender.execute(messageObject);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
