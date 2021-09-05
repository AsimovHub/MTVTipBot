package ac.asimov.mtvtipbot.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TransactionDao extends CrudRepository<Transaction, Long> {

    Optional<Transaction> findById(Long id);
    Optional<Transaction> findByTransactionHash(String transactionHash);
}
