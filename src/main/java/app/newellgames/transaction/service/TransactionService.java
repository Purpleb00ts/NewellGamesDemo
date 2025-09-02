package app.newellgames.transaction.service;

import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.model.TransactionStatus;
import app.newellgames.transaction.model.TransactionType;
import app.newellgames.transaction.repository.TransactionRepository;
import app.newellgames.user.model.User;
import app.newellgames.utility.UuidUtility;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Initialization of Transaction and saving it to DB
    public void initializeTransaction(User user, BigDecimal amount, TransactionStatus status, TransactionType type) {
        Transaction transaction = Transaction.builder()
                .owner(user)
                .amount(amount)
                .createdOn(LocalDateTime.now())
                .completedOn(LocalDateTime.now())
                .status(status)
                .type(type)
                .build();

        transactionRepository.save(transaction);
    }

    // Gets all transactions that user made and orders them by date descending
    public List<Transaction> getAllTransactionsByUser(User user) {
        return transactionRepository.findByOwnerOrderByCreatedOnDesc(user);
    }

    // Gets all transactions that have been made on the app, used for Admin's Transaction tab where he can see all transactions
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedOnDesc();
    }

    // Gets transactions by User ID or Username, used in Transactions so users can search for specific transactions
    public List<Transaction> getTransactionByOwnerIdOrUsername(String searchParam) {
        if (UuidUtility.isValid(searchParam)) {
            if(!transactionRepository.findByOwnerIdOrderByCreatedOnDesc(UUID.fromString(searchParam)).isEmpty()) {
                return transactionRepository.findByOwnerIdOrderByCreatedOnDesc(UUID.fromString(searchParam));
            } else {
                return transactionRepository.findById(UUID.fromString(searchParam)).stream().toList();
            }
        } else {
            return transactionRepository.findByOwnerUsernameContainingIgnoreCaseOrderByCreatedOnDesc(searchParam);
        }
    }
}
