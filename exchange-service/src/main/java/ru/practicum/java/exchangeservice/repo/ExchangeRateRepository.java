package ru.practicum.java.exchangeservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.java.exchangeservice.model.ExchangeRate;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findTopByFromAndToOrderByTimestampDesc(String from, String to);

    @Query(value = """
            SELECT DISTINCT ON (to_currency)
                   id, from_currency, to_currency, rate, timestamp
            FROM exchange_schema.exchange_rates
            WHERE from_currency = :base
            ORDER BY to_currency, timestamp DESC
            """, nativeQuery = true)
    List<ExchangeRate> findLatestByBase(@Param("base") String base);
}
