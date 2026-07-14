package com.customer.account.dto;


public record TransactionRequest(Long accountId, double amount) {
}
