package com.customer.account.controller;

import com.customer.account.dto.OpenAccountResponse;
import com.customer.account.dto.UserInfoResponse;
import com.customer.account.entity.Account;
import com.customer.account.exceptionhandler.CustomerNotFoundException;
import com.customer.account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    /**
     * This api is used to open current account for existing customer.
     * if initial credit is not zero then it will create a transaction and
     * will call transaction service.
     * also possible validations added
     *
     * @param customerID
     * @param initialCredit
     * @return
     */
    @PostMapping(value = "/open", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> openAccount(@RequestParam(required = true) @Valid @Positive Long customerID,
                                              @RequestParam(required = true) @Valid @Min(0) double initialCredit) {
        try {
            Optional<OpenAccountResponse> openAccountResponse = accountService.openAccount(customerID, initialCredit);
            if (openAccountResponse.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(openAccountResponse);
            } else {
                // Account was not created, possibly due to invalid input or business rules
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Account could not be created for customer Id " + customerID);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * this api is to get account details for a specific customer passing the customerid,
     * which is primary key of customer_detail table which is in H2 database
     *
     * @param customerID
     * @return
     * @throws CustomerNotFoundException
     */
    @GetMapping(value = "/userInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoResponse> getAccountInfo(@RequestParam(required = true) @Valid @Positive Long customerID) throws CustomerNotFoundException {
        Optional<UserInfoResponse> userInfo = accountService.getAccountInfo(customerID);
        if (userInfo.isPresent()) {
            return ResponseEntity.ok(userInfo.get());
        }
        throw new CustomerNotFoundException();
    }
}
