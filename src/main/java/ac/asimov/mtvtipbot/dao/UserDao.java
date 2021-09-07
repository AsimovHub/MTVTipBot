package ac.asimov.mtvtipbot.dao;

import ac.asimov.mtvtipbot.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserDao extends CrudRepository<User, Long> {

    Optional<User> findByUserKey(String userKey);
    Optional<User> findByUsername(String userKey);
    Optional<User> findByPublicKey(String publicKey);

}
