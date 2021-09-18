package ac.asimov.mtvtipbot.service;

import ac.asimov.mtvtipbot.dao.TransactionDao;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.TransferRequestDto;
import ac.asimov.mtvtipbot.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TransactionService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionDao dao;

    public ResponseWrapperDto<Transaction> createTransaction(TransferRequestDto request, String transactionHash) {
        Transaction transaction = new Transaction();
        transaction.setTransactionHash(transactionHash);
        transaction.setTransferredAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
        transaction.setAmount(request.getAmount());
        transaction.setSenderWallet(request.getSender().getReceiverAddress());
        transaction.setReceiverWallet(request.getReceiver().getReceiverAddress());

        transaction = dao.save(transaction);

        return new ResponseWrapperDto<>(transaction);
    }
}
