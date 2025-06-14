package ru.practicum.java.accountsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.java.accountsservice.entity.Currency;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.entity.UserAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    List<UserAccount> findAllByUser(User user);
    Optional<UserAccount> findByUserAndCurrency(User user, Currency currency);
    boolean existsByUserAndBalanceGreaterThan(User user, BigDecimal threshold);
}
