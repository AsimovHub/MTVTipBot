package ac.asimov.mtvtipbot.commands;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.AccountBalanceDto;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.exceptions.TipBotErrorException;
import ac.asimov.mtvtipbot.helper.MessageFormatHelper;
import ac.asimov.mtvtipbot.helper.DefaultMessage;
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

import java.math.RoundingMode;

@Component
public class BalanceCommand implements IBotCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private MultiVACBlockchainGateway blockchainGateway;

    @Override
    public String getCommandIdentifier() {
        return "balance";
    }

    @Override
    public String getDescription() {
        return "Show your account balance";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(message.getChatId().toString());
        messageObject.setReplyToMessageId(message.getMessageId());

        try {
            if (!message.getChat().isUserChat()) {
                throw new TipBotErrorException(DefaultMessage.PRIVATE_CHAT_COMMAND_IN_PUBLIC_CHAT);
            }
            // DO YOUR STUFF

            Long userId = message.getFrom().getId();
            ResponseWrapperDto<User> userResponse = userService.getUserByUserId(userId);
            if (userResponse.hasErrors()) {
                throw new TipBotErrorException(DefaultMessage.CANNOT_LOAD_ACCOUNT_PLEASE_NOTIFY_DEV);
            } else {
                if (userResponse.getResponse() == null) {
                    throw new TipBotErrorException(DefaultMessage.NO_ACCOUNT_PLEASE_REGISTER);
                } else {
                    ResponseWrapperDto<AccountBalanceDto> mtvBalanceResponse = blockchainGateway.getMTVAccountBalance(new WalletAccountDto(null, userResponse.getResponse().getPublicKey()));
                    ResponseWrapperDto<AccountBalanceDto> isaacBalanceResponse = blockchainGateway.getISAACAccountBalance(new WalletAccountDto(null, userResponse.getResponse().getPublicKey()));
                    if (mtvBalanceResponse.hasErrors() && isaacBalanceResponse.hasErrors()) {
                        throw new TipBotErrorException("Error while fetching account balance");
                    } else {
                        String messageString = "Balances of your Wallet is:\n";
                        if (mtvBalanceResponse.hasErrors() || mtvBalanceResponse.getResponse() == null || mtvBalanceResponse.getResponse().getAmount() == null) {
                            messageString += "$MTV Error\n";
                        } else {
                            messageString += mtvBalanceResponse.getResponse().getAmount().setScale(6, RoundingMode.HALF_UP).toString() + " $MTV\n";
                        }
                        if (isaacBalanceResponse.hasErrors() || isaacBalanceResponse.getResponse() == null || isaacBalanceResponse.getResponse().getAmount() == null) {
                            messageString += "$ISAAC Error";
                        } else {
                            messageString += isaacBalanceResponse.getResponse().getAmount().setScale(6, RoundingMode.HALF_UP).toString() + " $ISAAC\n";
                        }
                        messageString += "\n\nPlease note only $MTV & $ISAAC are supported. Do not send other funds to this wallet.";
                        messageObject.enableMarkdown(true);
                        messageObject.setText(MessageFormatHelper.appendDisclaimerAndEscapeMarkdownV1(messageString, true));
                    }

                }
            }
            try {
                absSender.execute(messageObject);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                throw new TipBotErrorException(DefaultMessage.SERVER_ERROR);
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
