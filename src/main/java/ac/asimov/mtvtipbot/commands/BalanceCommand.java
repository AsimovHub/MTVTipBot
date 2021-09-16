package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.AccountBalanceDto;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
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

public class BalanceCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

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
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());
        messageObject.enableMarkdownV2(true);

        if (message.getChat().isUserChat()) {
            // DO YOUR STUFF

            try {
                String messageString;
                Long userId = absSender.getMe().getId();
                ResponseWrapperDto<User> userResponse = userService.getUserByUserId(userId);
                if (userResponse.hasErrors()) {
                    messageString = "Error during account initialization. Please notify developer";
                } else {
                    if (userResponse.getResponse() == null) {
                        messageString = "You do not have an account yet. Please use /register";
                    } else {
                        ResponseWrapperDto<AccountBalanceDto> balanceResponse = blockchainGateway.getAccountBalance(new WalletAccountDto(null, userResponse.getResponse().getPublicKey()));
                        if (balanceResponse.hasErrors()) {
                            messageString = "Error while fetching account balance";
                        } else {
                            messageString = "Balance of your Wallet is " + balanceResponse.getResponse().getAmount().toString() + " $MTV";
                        }
                    }
                }
                messageObject.setText(MessageFormatHelper.appendDisclaimer(messageString, true));
            } catch (TelegramApiException e) {
                e.printStackTrace();
                messageObject.setText(MessageFormatHelper.escapeString("Cannot determine userdata"));
            }

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
