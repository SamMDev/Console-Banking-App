package com.sammdev.service;

import com.sammdev.db.dao.BalanceDao;
import com.sammdev.db.dao.CustomerDao;
import com.sammdev.db.dao.PaymentDao;
import com.sammdev.db.model.Customer;
import com.sammdev.db.model.Payment;
import com.sammdev.enums.PaymentHistoryType;
import com.sammdev.exception.FailedToCreateRecordException;
import com.sammdev.exception.FailedToUpdateRecordException;
import com.sammdev.exception.LoginFailedException;
import com.sammdev.exception.NotEnoughMoneyException;
import com.sammdev.exception.RecordNotFoundException;

import java.util.Collections;
import java.util.List;

public class CustomerService {

    private final CustomerDao customerDao = CustomerDao.getInstance();
    private final BalanceDao balanceDao = BalanceDao.getInstance();
    private final PaymentDao paymentDAO = PaymentDao.getInstance();


    /**
     * Finds a customer by matching his username (email) and password
     *
     * @param email    login username
     * @param password user password
     * @return
     * @throws LoginFailedException failed to login user
     */
    public Customer login(String email, String password) throws LoginFailedException {
        try {
            Customer user = customerDao.findCustomer(email);
            if (user.getPassword().equals(password)) {
                return user;
            } else {
                throw new LoginFailedException("Failed to authenticate user");
            }
        } catch (RecordNotFoundException e) {
            System.err.println(e.getMessage());
            // we do not want to show user why his login failed, we do not want to expose our internal logic
            throw new LoginFailedException("Failed to login user");
        }
    }


    /**
     * Check if user exists in local database
     *
     * @param email user email
     * @return true if user already exists
     */
    public boolean userExists(String email) {
        try {
            customerDao.findCustomer(email);
            return true;
        } catch (RecordNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if user exists in local database
     *
     * @param id    id of current user
     * @return      true if there is a user with such id
     */
    public boolean userExists(int id) {
        try {
            customerDao.findCustomer(id);
            return true;
        } catch (RecordNotFoundException e) {
            return false;
        }
    }


    /**
     * Registers new customer
     *
     * @param newCustomer customer information
     */
    public void registerCustomer(Customer newCustomer) throws FailedToCreateRecordException {
        try {
            customerDao.createCustomer(newCustomer);
            balanceDao.createBalance(newCustomer.getId(), 0);
        } catch (FailedToCreateRecordException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }


    /**
     * Checks if the customer has enough money and performs a withdrawal
     *
     * @param customer customer
     * @param amount   money to withdraw
     * @throws RecordNotFoundException there is no such record in balance table
     * @throws NotEnoughMoneyException customer does not have enough cash
     */
    public void withdraw(Customer customer, double amount) throws RecordNotFoundException, NotEnoughMoneyException {
        Double customerBalance = balanceDao.getBalance(customer.getId());
        if (amount > customerBalance) {
            throw new NotEnoughMoneyException("Customer does not have enough money!");
        }
        //subtracts money from your account
        balanceDao.updateBalance(customer.getId(), customerBalance - amount);
    }

    /**
     * Adds money to customer's account
     *
     * @param customer customer
     * @param amount   money
     * @throws RecordNotFoundException there is no such record on the balance table
     */
    public void deposit(Customer customer, double amount) throws RecordNotFoundException {
        Double balance = balanceDao.getBalance(customer.getId());
        balanceDao.updateBalance(customer.getId(), balance + amount);
    }

    /**
     * Transfers money from one user to another
     *
     * @param sender   who sends money
     * @param receiver receiver of the money
     * @param amount   amount
     * @throws RecordNotFoundException record wes not found in db
     * @throws NotEnoughMoneyException amount exceeded senders balance
     */
    public void transfer(Customer sender, Customer receiver, double amount) throws RecordNotFoundException, NotEnoughMoneyException {
        Double senderBalance = balanceDao.getBalance(sender.getId());
        Double targetBalance = balanceDao.getBalance(receiver.getId());

        //sender must have enough money
        if (amount > senderBalance) {
            throw new NotEnoughMoneyException("You don't have enough money to transfer!");
        }

        if(userExists(receiver.getId())){
            // add money to target
            balanceDao.updateBalance(receiver.getId(), amount + targetBalance);
             //subtract money from sender
            balanceDao.updateBalance(sender.getId(), senderBalance - amount);
        }
    }

    /**
     * Updates user password
     *
     * @param customer    customer for which password is changed
     * @param newPassword new password
     */
    public void changePassword(Customer customer, String newPassword) {
        String oldPassword = customer.getPassword();
        customer.setPassword(newPassword);
        try {
            customerDao.updateCustomer(customer);
        } catch (Exception e) {
            System.err.println("Failed to update pasword!");
            customer.setPassword(oldPassword);
            throw new FailedToUpdateRecordException("Failed to update password!");
        }
    }

    /**
     * Gets customers current balance
     *
     * @param customer customer
     * @return actual balance of the customer
     * @throws RecordNotFoundException no such record
     */
    public double getCurrentBalance(Customer customer) throws RecordNotFoundException {
        return balanceDao.getBalance(customer.getId());
    }


    public int getHighestCustomerId() {
        return customerDao.getLastId();
    }


    /**
     * Gets list of payments (sent payments or received payments)
     *
     * @param customer              Payments from customer account
     * @param paymentHistoryType    Selecting sent or received payments
     * @return                      List of chosen payments or empty list
     */
    public List<Payment> getListOfPayments(Customer customer, PaymentHistoryType paymentHistoryType) {
        if (paymentHistoryType.equals(PaymentHistoryType.RECEIVED)) {
            return paymentDAO.getReceivedPayments(customer.getId());
        } else if (paymentHistoryType.equals(PaymentHistoryType.SEND)) {
            return paymentDAO.getSentPayments(customer.getId());
        } else {
            System.err.println("Wrong payment type!");
            return Collections.emptyList();
        }
    }

    /**
     * Getting customer with given id
     *
     * @param id                        id of wanted customer
     * @return                          returns customer
     * @throws RecordNotFoundException  if id does not match with any registered customer
     */
    public Customer getCustomer(int id) throws RecordNotFoundException {
        try {
            return customerDao.findCustomer(id);
        } catch (RecordNotFoundException e) {
            System.err.println(e);
            throw e;
        }
    }

    /**
     * Registers payment, inserting it into database
     *
     * @param sender
     * @param receiver
     * @param amount
     * @param time
     * @param date
     */
    public void registerPayment(Customer sender, Customer receiver, double amount, String time, String date) {
        try {
            paymentDAO.createPayment(
                    sender.getId(),
                    receiver.getId(),
                    amount,
                    time,
                    date
            );
        } catch (FailedToCreateRecordException e) {
            System.err.println(e);
        }
    }
}
