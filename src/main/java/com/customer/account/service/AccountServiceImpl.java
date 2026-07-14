package com.customer.account.service;

import com.customer.account.dto.AccountInfo;
import com.customer.account.dto.OpenAccountResponse;
import com.customer.account.dto.TransactionInfo;
import com.customer.account.dto.TransactionRequest;
import com.customer.account.dto.UserInfoResponse;
import com.customer.account.entity.Account;
import com.customer.account.entity.CustomerDetail;
import com.customer.account.exceptionhandler.ConnectivityException;
import com.customer.account.exceptionhandler.CustomerNotFoundException;
import com.customer.account.repository.AccountRepository;
import com.customer.account.repository.CustomerDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerDetailRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${transaction.service.url}")
    private String transactionServiceUrl;

    public AccountServiceImpl() {
    }

    /**
     * This method is used to open current account for existing customer.
     * if initial credit is not zero then it will create a transaction and
     * will call transaction service.
     * @param customerId
     * @param initialCredit
     * @return
     * @throws CustomerNotFoundException
     * @throws ConnectivityException
     */
    @Transactional
    public Optional<OpenAccountResponse> openAccount(Long customerId, double initialCredit) throws CustomerNotFoundException, ConnectivityException {

        CustomerDetail customerDetail = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerId : " + customerId));

        Account account = new Account();
        account.setCustomerDetail(customerDetail);
        account.setBalance(initialCredit);
        Account savedAccount = accountRepository.save(account);

        createTransaction(customerId, initialCredit, account);

        return Optional.of(new OpenAccountResponse(savedAccount.getId(), customerDetail.getId(), "Account created successfully"));
    }

    /**
     * this method to create transaction-service from account service to create a transaction
     *
     * @param customerId
     * @param initialCredit
     * @param account
     * @throws ConnectivityException
     */
    @Transactional
    public void createTransaction(Long customerId, double initialCredit, Account account) throws ConnectivityException {
        if (initialCredit > 0) {
            TransactionRequest transactionRequest = new TransactionRequest(account.getId(), initialCredit);
            try {
                log.info("calling transaction service to create transaction for this account");
                restTemplate.postForObject(transactionServiceUrl + "/create", transactionRequest, String.class);
            } catch (RestClientException e) {
                log.error("error occurred while connecting transaction service "+e.getMessage());
                throw new ConnectivityException("Please try again later to open account for customer : " + customerId);
            }
        }
    }

    /**
     * this method is to get account details for a specific customer passing the customerid,
     * which is primary key of customer_detail table
     *
     * @param customerId
     * @return
     * @throws CustomerNotFoundException
     */
    public Optional<UserInfoResponse> getAccountInfo(Long customerId) throws CustomerNotFoundException {

        CustomerDetail customerDetail = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerId : " + customerId));

        UserInfoResponse response = new UserInfoResponse();
        response.setName(customerDetail.getName());
        response.setSurname(customerDetail.getSurname());

        double totalBalance = 0.0;
        List<Account> accounts = accountRepository.findByCustomerDetail(customerDetail);

        for (Account account : accounts) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAccountId(account.getId());
            accountInfo.setBalance(account.getBalance());
            totalBalance += account.getBalance();

            // Retrieve transactions for each account from the Transaction Service
            List<TransactionInfo> transactionInfoList = getTransactionInfo(account);

            accountInfo.setTransactions(transactionInfoList);
            response.getAccounts().add(accountInfo);
        }

        response.setBalance(totalBalance);
        return Optional.of(response);
    }

    // Retrieve transactions for each account from the Transaction Service
    private List<TransactionInfo> getTransactionInfo(Account account) {
        ResponseEntity<List<TransactionInfo>> transactionInfoListResp = null;
        try {
            log.info("calling transaction service to get transaction info for this account");
            transactionInfoListResp = restTemplate.exchange(
                    transactionServiceUrl + "/account/" + account.getId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            log.error("Issue occurred with connecting transaction microservice "+e.getMessage());
        }

        List<TransactionInfo> transactionInfoList = new ArrayList<>();
        if (transactionInfoListResp != null && transactionInfoListResp.getStatusCode().is2xxSuccessful()) {
            transactionInfoList = transactionInfoListResp.getBody();
        }
        return transactionInfoList;
    }
}
