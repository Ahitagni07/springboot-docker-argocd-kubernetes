package com.customer.account.exceptionhandler;

public class ConnectivityException extends Exception {
    public ConnectivityException(String message) {
        super(message);
    }

    public ConnectivityException() {

    }
}
