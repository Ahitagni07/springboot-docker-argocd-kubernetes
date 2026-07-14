package com.customer.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo {
    private Long accountId;
    private double balance;
    private List<TransactionInfo> transactions = new ArrayList<>();

    // Getters and setters
}
