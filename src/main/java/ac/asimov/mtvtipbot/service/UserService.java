package ac.asimov.mtvtipbot.service;

import ac.asimov.mtvtipbot.dao.UserDao;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.model.Transaction;
import ac.asimov.mtvtipbot.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserDao dao;

    public ResponseWrapperDto<User> createUser(String key, String username, String privateKey, String salt, String publicKey) {

        // TODO

        return new ResponseWrapperDto("Not implemented yet");
    }

    public ResponseWrapperDto<User> updateUsername(User user, String username) {
        try {
            user.setUsername(username);
            user = dao.save(user);
            return new ResponseWrapperDto<>(user);
        } catch (Exception e) {
            e.printStackTrace();
             return new ResponseWrapperDto("Cannot update username to \"" + username + "\" for user with key \"" + user.getUserKey() + "\"");
         }
    }

    public ResponseWrapperDto<Transaction> createTransaction() {

        // TODO

        return new ResponseWrapperDto("Not implemented yet");
    }

    public ResponseWrapperDto<User> getUserByUserId(Long userId) {

        // TODO

        return new ResponseWrapperDto("Not implemented yet");
    }

    public ResponseWrapperDto<User> getUserByUsername(String username) {

        // TODO

        return new ResponseWrapperDto("Not implemented yet");
    }

    public boolean doesUserIdExist(Long userId) {
        ResponseWrapperDto<User> result = getUserByUserId(userId);
        if (result.hasErrors()) {
            return false;
        }
        return result.getResponse() != null && result.getResponse().getId() != null;
    }

    public boolean doesUserNameExist(String username) {
        ResponseWrapperDto result = getUserByUsername(username);
        if (result.hasErrors()) {
            return false;
        }
        return result.getResponse() != null;
    }

    public ResponseWrapperDto<WalletAccountDto> getWalletAddressByUserId(Long userId) {
        ResponseWrapperDto<User> userResult = getUserByUserId(userId);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto("User does not exist");
        }

        User user = userResult.getResponse();

        return new ResponseWrapperDto(new WalletAccountDto(null, user.getPublicKey()));
    }

    public ResponseWrapperDto<WalletAccountDto> getWalletAddressByUsername(String username) {
        ResponseWrapperDto<User> userResult = getUserByUsername(username);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto("User does not exist");
        }

        User user = userResult.getResponse();

        return new ResponseWrapperDto(new WalletAccountDto(null, user.getPublicKey()));
    }

    public ResponseWrapperDto<WalletAccountDto> getPrivateKeyByUserId(Long userId) {
        ResponseWrapperDto<User> userResult = getUserByUserId(userId);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto("User does not exist");
        }

        User user = userResult.getResponse();

        return new ResponseWrapperDto(new WalletAccountDto(null, user.getPublicKey()));
    }

    public ResponseWrapperDto<WalletAccountDto> getFullWalletAccountByUserId(Long userId) {
        ResponseWrapperDto<User> userResult = getUserByUserId(userId);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto("User does not exist");
        }

        User user = userResult.getResponse();

        return new ResponseWrapperDto(new WalletAccountDto(null, user.getPublicKey()));
    }
}
