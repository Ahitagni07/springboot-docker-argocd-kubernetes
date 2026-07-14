package com.customer.account.service;

import com.customer.account.dto.OpenAccountResponse;
import com.customer.account.dto.UserInfoResponse;
import com.customer.account.entity.Account;
import com.customer.account.exceptionhandler.ConnectivityException;
import com.customer.account.exceptionhandler.CustomerNotFoundException;

import java.util.Optional;

public interface AccountService {
    Optional<OpenAccountResponse> openAccount(Long customerId, double initialCredit) throws CustomerNotFoundException, ConnectivityException;

    Optional<UserInfoResponse> getAccountInfo(Long customerId) throws CustomerNotFoundException;
}
