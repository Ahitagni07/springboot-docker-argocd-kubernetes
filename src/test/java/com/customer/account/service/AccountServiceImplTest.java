package com.customer.account.service;

import com.customer.account.dto.TransactionInfo;
import com.customer.account.dto.TransactionRequest;
import com.customer.account.dto.UserInfoResponse;
import com.customer.account.entity.Account;
import com.customer.account.entity.CustomerDetail;
import com.customer.account.exceptionhandler.ConnectivityException;
import com.customer.account.exceptionhandler.CustomerNotFoundException;
import com.customer.account.repository.AccountRepository;
import com.customer.account.repository.CustomerDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerDetailRepository customerDetailRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountServiceImpl accountService = new AccountServiceImpl();

    @Test
    void testOpenAccount_success() throws CustomerNotFoundException, ConnectivityException {
        Long customerId = 1L;
        double initialCredit = 1000.0;

        CustomerDetail customerDetail = new CustomerDetail();
        customerDetail.setId(customerId);
        customerDetail.setName("John");
        customerDetail.setSurname("Doe");

        Account account = new Account();
        account.setId(1L);
        account.setBalance(initialCredit);
        account.setCustomerDetail(customerDetail);

        when(customerDetailRepository.findById(customerId)).thenReturn(Optional.of(customerDetail));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.openAccount(customerId, initialCredit);

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(restTemplate, times(1)).postForObject(anyString(), any(TransactionRequest.class), eq(String.class));
    }

    @Test
    void testOpenAccount_customerNotFound() {
        Long customerId = 999L;
        double initialCredit = 1000.0;

        when(customerDetailRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> accountService.openAccount(customerId, initialCredit));
    }

    @Test
    void testOpenAccount_connectivityException() throws CustomerNotFoundException {
        Long customerId = 1L;
        double initialCredit = 1000.0;

        CustomerDetail customerDetail = new CustomerDetail();
        customerDetail.setId(customerId);
        customerDetail.setName("John");
        customerDetail.setSurname("Doe");

        Account account = new Account();
        account.setId(1L);
        account.setBalance(initialCredit);
        account.setCustomerDetail(customerDetail);

        when(customerDetailRepository.findById(customerId)).thenReturn(Optional.of(customerDetail));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        doThrow(new RestClientException("Connectivity Error")).when(restTemplate).postForObject(anyString(), any(TransactionRequest.class), eq(String.class));

        assertThrows(ConnectivityException.class, () -> accountService.openAccount(customerId, initialCredit));
    }

    @Test
    void testGetAccountInfo_success() throws CustomerNotFoundException {
        Long customerId = 1L;

        CustomerDetail customerDetail = new CustomerDetail();
        customerDetail.setId(customerId);
        customerDetail.setName("John");
        customerDetail.setSurname("Doe");

        Account account1 = new Account();
        account1.setId(1L);
        account1.setBalance(500.0);
        account1.setCustomerDetail(customerDetail);

        Account account2 = new Account();
        account2.setId(2L);
        account2.setBalance(1500.0);
        account2.setCustomerDetail(customerDetail);

        List<Account> accounts = List.of(account1, account2);

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTrxId(1L);
        transactionInfo.setAmount(500.0);

        ResponseEntity<List<TransactionInfo>> transactionResponse = ResponseEntity.ok(List.of(transactionInfo));

        when(customerDetailRepository.findById(customerId)).thenReturn(Optional.of(customerDetail));
        when(accountRepository.findByCustomerDetail(customerDetail)).thenReturn(accounts);
        when(restTemplate.exchange(
                anyString(),
                Mockito.any(HttpMethod.class),
                Mockito.any(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn(transactionResponse);

        Optional<UserInfoResponse> response = accountService.getAccountInfo(customerId);

        assertTrue(response.isPresent());
        assertEquals(2, response.get().getAccounts().size());
        assertEquals(2000.0, response.get().getBalance());
    }

    @Test
    void testGetAccountInfo_customerNotFound() {
        Long customerId = 999L;

        when(customerDetailRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> accountService.getAccountInfo(customerId));
    }

    @Test
    void testGetAccountInfo_transactionServiceFailure() throws CustomerNotFoundException {
        Long customerId = 1L;

        CustomerDetail customerDetail = new CustomerDetail();
        customerDetail.setId(customerId);
        customerDetail.setName("Ahitagni");
        customerDetail.setSurname("Saha");

        Account account = new Account();
        account.setId(1L);
        account.setBalance(500.0);
        account.setCustomerDetail(customerDetail);

        List<Account> accounts = List.of(account);

        when(customerDetailRepository.findById(customerId)).thenReturn(Optional.of(customerDetail));
        when(accountRepository.findByCustomerDetail(customerDetail)).thenReturn(accounts);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Transaction Service failed"));

        assertThrows(RuntimeException.class, () -> accountService.getAccountInfo(customerId));
    }

    @Test
    void testCreateTransaction_connectivityException() {
        Long customerId = 1L;
        double initialCredit = 1000.0;

        Account account = new Account();
        account.setId(1L);
        account.setBalance(initialCredit);

        Mockito.when((restTemplate).postForObject(anyString(), any(TransactionRequest.class), eq(String.class))).thenThrow(new RuntimeException("Connectivity error"));

        assertThrows(RuntimeException.class, () -> accountService.createTransaction(customerId, initialCredit, account));
    }
}
