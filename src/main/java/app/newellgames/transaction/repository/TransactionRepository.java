package app.newellgames.transaction.repository;

import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.model.TransactionType;
import app.newellgames.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByOwnerOrderByCreatedOnDesc(User user);
    List<Transaction> findAllByOrderByCreatedOnDesc();

    List<Transaction> findByOwnerIdOrderByCreatedOnDesc(UUID id);

    @Query("SELECT t FROM Transaction t WHERE LOWER(t.owner.username) LIKE LOWER(CONCAT('%', :username, '%')) ORDER BY t.createdOn DESC")
    List<Transaction> findByOwnerUsernameContainingIgnoreCaseOrderByCreatedOnDesc(String username);

}
