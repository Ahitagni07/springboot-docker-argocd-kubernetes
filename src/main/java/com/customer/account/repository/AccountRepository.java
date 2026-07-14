package com.customer.account.repository;

import com.customer.account.entity.Account;
import com.customer.account.entity.CustomerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerDetail(CustomerDetail customerDetail);
}
