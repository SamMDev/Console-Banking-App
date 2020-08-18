package com.sammdev.PreCreation;

import com.sammdev.db.model.Customer;
import com.sammdev.exception.FailedToCreateRecordException;
import com.sammdev.service.BankingService;
import com.sammdev.service.CustomerService;

import static com.sammdev.PreCreation.RandomNames.*;

public class PreCreation {
    private CustomerService customerService;
    private BankingService bankingService;

    private int numOfNewCustomers = 10;

    public PreCreation(){
        customerService = new CustomerService();
        bankingService = new BankingService();
    }

    /**
     * Generates unique, unregistered email
     *
     * @param firstName     First name of customer
     * @param lastName      Last name of customer
     * @return              His new email
     */

    private String generateEmail(String firstName, String lastName){
        String email = (firstName + '_' + lastName).toLowerCase();

        String numCombination;
        do{
            numCombination = "";
            for(int i = 0; i<4; i++){
                numCombination += (int)(Math.random()*10);
            }
        }while(customerService.userExists(email + numCombination + "@gmail.com"));

        return email + numCombination + "@gmail.com";
    }

    /**
     * Creates Customer object
     * @param customerId    current id for customer who is going to be registered
     * @return              new Customer
     */
    private Customer generateCustomer(int customerId){
        int id = customerId;
        String firstName = FIRST_NAMES[(int)(Math.random()*(FIRST_NAMES.length-1))];
        String lastName = LAST_NAMES[(int)(Math.random()*(LAST_NAMES.length-1))];
        String email = generateEmail(firstName, lastName);
        String address = "";
        String password = bankingService.getHash("admin");

        return new Customer(id, firstName, lastName, email, address, password);
    }



    /**
     * Inserts random user accounts into database
     */
    public void preCreateCustomers(){
        int currentUserId = customerService.getHighestCustomerId() + 1;
        for(int i = 0; i<numOfNewCustomers; i++, currentUserId++){
            try{
                customerService.registerCustomer(generateCustomer(currentUserId));
            }catch (FailedToCreateRecordException e){
                System.err.println(e);
            }
        }
    }
}
