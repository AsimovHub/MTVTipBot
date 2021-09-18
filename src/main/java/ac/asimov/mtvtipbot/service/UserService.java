package ac.asimov.mtvtipbot.service;

import ac.asimov.mtvtipbot.dao.UserDao;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.helper.CryptoHelper;
import ac.asimov.mtvtipbot.model.Transaction;
import ac.asimov.mtvtipbot.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserDao dao;

    @Transactional
    public ResponseWrapperDto<User> createUser(String key, String username, String privateKey, String salt, String publicKey) {
        User user = new User();
        user.setCreatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
        user.setUserKey(key);
        user.setUsername(username);
        user.setPrivateKey(privateKey);
        user.setSalt(salt);
        user.setPublicKey(publicKey);
        // TODO
        user = dao.save(user);
        return new ResponseWrapperDto<>(user);
    }

    @Transactional
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

    public ResponseWrapperDto<User> getUserByUserId(Long userId) {
        try {
            String key = CryptoHelper.userIdToKey(userId);
            Optional<User> optionalUser = dao.findByUserKey(key);

            if (optionalUser.isEmpty()) {
                return new ResponseWrapperDto<>();
            } else {
                return new ResponseWrapperDto<>(optionalUser.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("Cannot load user");
        }
    }

    public ResponseWrapperDto<User> getUserByUsername(String username) {

        try {
            Optional<User> optionalUser = dao.findByUsername(username);

            if (optionalUser.isEmpty()) {
                return new ResponseWrapperDto<>();
            } else {
                return new ResponseWrapperDto<>(optionalUser.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("Cannot load user");
        }
    }

    public boolean doesUserIdExist(Long userId) {
        ResponseWrapperDto<User> result = getUserByUserId(userId);
        if (result.hasErrors()) {
            return false;
        }
        return result.getResponse() != null;
    }

    public boolean doesUserNameExist(String username) {
        ResponseWrapperDto<User> result = getUserByUsername(username);
        if (result.hasErrors()) {
            return false;
        }
        return result.getResponse() != null;
    }

    public ResponseWrapperDto<WalletAccountDto> getWalletAddressByUserId(Long userId) {
        ResponseWrapperDto<User> userResult = getUserByUserId(userId);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto<>(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto<>("User does not exist");
        }

        User user = userResult.getResponse();

        return new ResponseWrapperDto<>(new WalletAccountDto(null, user.getPublicKey()));
    }

    public ResponseWrapperDto<WalletAccountDto> getWalletAddressByUsername(String username) {
        ResponseWrapperDto<User> userResult = getUserByUsername(username);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto<>(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto<>("User does not exist");
        }

        User user = userResult.getResponse();

        return new ResponseWrapperDto<>(new WalletAccountDto(null, user.getPublicKey()));
    }

    public ResponseWrapperDto<WalletAccountDto> getPrivateKeyByUserId(Long userId) {
        ResponseWrapperDto<User> userResult = getUserByUserId(userId);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto<>(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto<>("User does not exist");
        }

        User user = userResult.getResponse();
        try {
            String decryptedPrivateKey = CryptoHelper.decrypt(user.getPrivateKey(), userId + "", user.getSalt());
            return new ResponseWrapperDto<>(new WalletAccountDto(decryptedPrivateKey, null));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("Cannot decrypt private Key");
        }
    }

    public ResponseWrapperDto<WalletAccountDto> getFullWalletAccountByUserId(Long userId) {
        ResponseWrapperDto<User> userResult = getUserByUserId(userId);
        if (userResult.hasErrors()) {
            return new ResponseWrapperDto<>(userResult.getErrorMessage());
        }

        if (userResult.getResponse() == null) {
            return new ResponseWrapperDto<>("User does not exist");
        }

        User user = userResult.getResponse();

        try {
            String decryptedPrivateKey = CryptoHelper.decrypt(user.getPrivateKey(), userId + "", user.getSalt());
            return new ResponseWrapperDto<>(new WalletAccountDto(decryptedPrivateKey, user.getPublicKey()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("Cannot decrypt private Key");
        }
    }
}
