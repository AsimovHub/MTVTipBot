package ac.asimov.mtvtipbot.bots;

import ac.asimov.mtvtipbot.commands.*;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.model.User;
import ac.asimov.mtvtipbot.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserService userService;

    @Autowired
    public MTVTipBot(HelpCommand helpCommand,
                     InfoCommand infoCommand,
                     StartCommand startCommand,
                     RegisterCommand registerCommand,
                     MTVTipCommand mtvTipCommand,
                     MTVSendCommand mtvSendCommand,
                     ISAACTipCommand isaacTipCommand,
                     ISAACSendCommand isaacSendCommand,
                     AccountCommand accountCommand,
                     BalanceCommand balanceCommand,
                     WithdrawCommand withdrawCommand,
                     DonateCommand donateCommand,
                     FaucetCommand faucetCommand) {
        logger.info("Creating tipbot commands");

        register(helpCommand);
        register(infoCommand);

        register(startCommand);
        register(registerCommand);

        register(mtvTipCommand);
        register(mtvSendCommand);

        register(isaacTipCommand);
        register(isaacSendCommand);

        register(accountCommand);
        register(balanceCommand);
        register(withdrawCommand);

        register(donateCommand);
        register(faucetCommand);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (!update.getMessage().getFrom().getIsBot()) {
            Long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();

            ResponseWrapperDto<User> userResponse = userService.getUserByUserId(userId);

            if (!userResponse.hasErrors() && userResponse.getResponse() != null) {
                User user = userResponse.getResponse();
                if (!StringUtils.equals(username, user.getUsername())) {
                    userService.updateUsername(user, username);
                }
            }
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

}
