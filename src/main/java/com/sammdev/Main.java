package com.sammdev;

import com.sammdev.preCreation.PreCreation;
import com.sammdev.service.BankingService;

public class Main {

    public static void main(String[] args) {
        new PreCreation().preCreateCustomers();
        new BankingService().defaultChoice();
    }
}
