package ac.asimov.mtvtipbot.bots;

import ac.asimov.mtvtipbot.commands.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MTVTipBot extends TelegramLongPollingCommandBot {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${tipbot.username}")
    private String username;
    @Value("${tipbot.token}")
    private String token;

    public MTVTipBot() {
        logger.info("Creating TipBotCommand");

        register(new StartCommand("start", "Start the bot"));
        register(new HelpCommand("help", "Show this help"));
        register(new InfoCommand("info", "Get more information about this tip bot"));


        register(new RegisterCommand("register", "Setup a wallet for your Telegram account"));
        register(new MTVTipCommand("mtvtip", "Send given tip amount to replied user"));

        register(new AccountCommand("account", "Show your account address"));
        register(new BalanceCommand("balance", "Show your account balance"));

        register(new SendCommand("mtvsend", "Send given tip amount to the given user"));
        register(new WithdrawCommand("withdraw", "Withdraw given amount of MTV to your given wallet"));
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void processNonCommandUpdate(Update update) {

        // TODO: take a look user exist and username must be changed

    }

    @Override
    public String getBotToken() {
        return token;
    }

}
