package com.sammdev.enums;

import com.sammdev.exception.InputErrorException;

public enum PaymentHistoryType {
    SEND(1),
    RECEIVED(2);

    int value;

    PaymentHistoryType(int value) {
        this.value = value;
    }

    public static PaymentHistoryType fromValue(int value) throws InputErrorException {
        if (value == 1) return SEND;
        else if (value == 2) return RECEIVED;
        else throw new InputErrorException("Wrong choice!");
    }
}
