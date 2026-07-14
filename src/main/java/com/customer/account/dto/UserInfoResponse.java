package com.customer.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {

    private String name;
    private String surname;
    private double balance; // Total balance across all accounts
    private List<AccountInfo> accounts = new ArrayList<>();
}
