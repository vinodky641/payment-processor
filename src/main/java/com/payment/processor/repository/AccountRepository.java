package com.payment.processor.repository;

import com.payment.processor.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountId in :ids order by a.accountId")
    List<Account> lockAccounts(@Param("ids") Collection<String> ids);

    Optional<Account> findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);

}

