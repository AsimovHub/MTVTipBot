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
public class HelpCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getCommandIdentifier() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show this help";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {

        if (message.getFrom().getIsBot()) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("General commands:\n");
            sb.append("\n");
            sb.append("/help - Show this help\n");
            sb.append("/info - Get more information about this tip bot\n");
            sb.append("/faucet - Claim some free MTV from the Asimov faucet\n");
            sb.append("/donate [amount] - Donate given amount to the dev wallet\n");
            sb.append("/mtvtip [amount] - Send given amount of MTV to replied user\n");
            sb.append("/mtvsend [user] [amount] - Send given amount of MTV to the given user\n");
            sb.append("/mtvsend [user] [wallet] - Send given amount of MTV to the given wallet address\n");
            sb.append("\n");

            sb.append("Private Chat commands:\n");
            sb.append("\n");
            sb.append("/register - Setup a wallet for your Telegram account\n");
            sb.append("/balance - Show your account (wallet) balance\n");
            sb.append("/account - Show your account (wallet) address\n");
            sb.append("/withdraw [address] - Withdraw all your MTV to your given wallet\n");
            sb.append("/withdraw [address] [amount] - Withdraw given amount of MTV to your given wallet\n");


            String messageString = sb.toString();
            messageString = MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV2(messageString, true);



            SendMessage helpMessage = new SendMessage();
            helpMessage.setChatId(message.getChatId().toString());
            helpMessage.setReplyToMessageId(message.getMessageId());
            helpMessage.enableMarkdownV2(true);

            helpMessage.setText(messageString);
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
