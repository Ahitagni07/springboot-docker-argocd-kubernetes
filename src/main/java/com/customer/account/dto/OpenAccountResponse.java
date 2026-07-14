package com.customer.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenAccountResponse {
    private Long accountId;
    private Long customerId;
    private String status;
}
