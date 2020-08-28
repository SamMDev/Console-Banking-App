package com.sammdev.service;

import com.sammdev.db.model.Customer;
import com.sammdev.db.model.Payment;
import com.sammdev.enums.*;
import com.sammdev.exception.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class BankingService {

    private final Scanner scanner = new Scanner(System.in);
    private final CustomerService customerService;
    private Customer activeCustomer;

    public BankingService() {
        customerService = CustomerService.getInstance();
    }

    /**
     * Generates SHA-512 hash
     *
     * @param input     String you want to be hashed
     * @return          SHA-512 hash
     */
    public String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public void defaultChoice() {
        int choice;

        System.out.println(
                "________________________________________\n"
                        + "|Enter the choice: \n"
                        + "|Registration: (press 1)\n"
                        + "|Log in: (press 2)\n"
        );

        try {
            choice = scanner.nextInt();
            if (choice == 1) {
                registration();
            } else if (choice == 2) {
                logIn();
            } else {
                System.out.println("Incorrect input");
                defaultChoice();
            }
        } catch (Exception e) {
            System.out.println("Please enter only available options");
            scanner.nextLine();
            defaultChoice();
        }
    }

    private void registration() {
        Customer newCustomer = new Customer();
        newCustomer.setId(customerService.getHighestCustomerId() + 1);

        scanner.nextLine();

        while(true){
            String firstName = askUserToEnterValue("first name");
            try{
                if(InputType.NAME.isValid(firstName)){
                    newCustomer.setFirstName(firstName);
                    break;
                }else{
                    throw new IncorrectInputFormatException("Incorrect format");
                }
            }catch (IncorrectInputFormatException e){
                System.err.println(e.getMessage());
                registration();
            }
        }

        while(true){
            String lastName = askUserToEnterValue("last name");
            try{
                if(InputType.NAME.isValid(lastName)){
                    newCustomer.setLastName(lastName);
                    break;
                }else{
                    throw new IncorrectInputFormatException("Incorrect format");
                }
            }catch (IncorrectInputFormatException e){
                System.err.println(e.getMessage());
                registration();
            }
        }

        newCustomer.setAddress(askUserToEnterValue("address"));

        while(true){
            String email = askUserToEnterValue("email");
            try{
                if(customerService.userExists(email)){
                    System.out.println("This email is already registered");
                    registration();
                }else{
                    if(InputType.EMAIL.isValid(email)){
                        newCustomer.setEmail(email);
                        break;
                    }else{
                        throw new IncorrectInputFormatException("Incorrect email format");
                    }
                }
            }catch (IncorrectInputFormatException e){
                System.err.println(e.getMessage());
                registration();
            }
        }

        while(true){
            String password = askUserToEnterValue("password");
            try{
                if(InputType.PASSWORD.isValid(password)){
                    newCustomer.setPassword(getHash(password));
                    break;
                }else{
                    throw new IncorrectInputFormatException("Incorrect format \n" +
                            "Password must contain: \n" +
                            "At least 8 chars\n" +
                            "At least one digit\n" +
                            "At least one lower char and one upper char\n" +
                            "At least one char within a set of special chars (@#%$^ etc.)\n" +
                            "Does not contain space, tab, etc.");
                }
            }catch (IncorrectInputFormatException e){
                System.err.println(e.getMessage());
                registration();
            }
        }


        try {
            customerService.registerCustomer(newCustomer);
        } catch (FailedToCreateRecordException e) {
            System.err.println("Failed to register new customer: " + e.getMessage());
        }

        System.out.println("Registration has been successful");
        defaultChoice();
    }


    private void logIn() {
        String email, password;
        scanner.nextLine();
        email = askUserToEnterValue("email");
        password = getHash(askUserToEnterValue("password"));

        try {
            activeCustomer = customerService.login(email.toLowerCase(), password);
            customerChoices();
        } catch (LoginFailedException e) {
            System.out.println("Customer not found. E-mail or password is incorrect");
            defaultChoice();
        }
    }

    private void customerChoices() {
        int choice;

        System.out.println(
                "________________________________________\n"
                        + "|Enter the choice: \n"
                        + "|View balance on your account: (press 1)\n"
                        + "|Withdraw money: (press 2)\n"
                        + "|Deposit money: (press 3)\n"
                        + "|Make a payment: (press 4)\n"
                        + "|Show history of payments: (press 5)\n"
                        + "|Info about your account: (press 6)\n"
                        + "|Change password: (press 7)\n"
                        + "|Exit: (press 8)\n"
        );

        try {
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    showBalance();
                    break;
                case 2:
                    withdraw();
                    break;
                case 3:
                    deposit();
                    break;
                case 4:
                    payment();
                    break;
                case 5:
                    showHistoryOfPayments();
                    break;
                case 6:
                    accountInfo();
                    break;
                case 7:
                    passwordChange();
                    break;
                case 8:
                    activeCustomer = null;
                    System.out.println("You are logged out!");
                    defaultChoice();
                    break;
                default:
                    incorrectInput("Incorrect input2");
                    customerChoices();
            }
        } catch (Exception e) {
            scanner.nextLine();
            System.err.println(e);
            incorrectInput("Incorrect input3");
            customerChoices();
        }
    }

    private void withdraw() {
        double amount = 0;
        System.out.println("Please enter the amount of money you want to withdraw: ");
        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            scanner.nextLine();
            incorrectInput("You entered invalid input");
            withdraw();
        }
        if (amount > 0) {
            try {
                customerService.withdraw(activeCustomer, amount);
                System.out.println("Withdraw has been successful");
            } catch (RecordNotFoundException e) {
                System.err.println("Failed to make withdrawal, so sad :(");
            } catch (NotEnoughMoneyException e) {
                System.out.println("Not enough money on your account");
            } finally {
                customerChoices();
            }
        } else {
            incorrectInput("Selection must be higher than 0");
            withdraw();
        }
    }

    private void deposit() {
        double amount = 0;

        System.out.println("Please enter the amount you would like to deposit: ");

        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            scanner.nextLine();
            incorrectInput("incorrect deposit input");
            deposit();
        }
        if (amount > 0) {
            try {
                customerService.deposit(activeCustomer, amount);
                System.out.println("Deposit has been successful!");
            } catch (RecordNotFoundException e) {
                System.err.println("Failed to make deposit, fnuk :(");
            } finally {
                customerChoices();
            }

        } else {
            incorrectInput("incorrect deposit amount");
            deposit();
        }
    }


    private void showBalance() {
        try {
            System.out.println(
                    "Your balance is: " +
                            customerService.getCurrentBalance(activeCustomer)
            );
        } catch (RecordNotFoundException e) {
            System.err.println("Failed to load the balance :(");
        } finally {
            customerChoices();
        }
    }

    private void payment() {
        Customer reciever = null;
        int enteredRecieverId;
        double amount = 0;

        System.out.println("Enter reciever's ID: ");

        try {
            //read receiver's id
            enteredRecieverId = scanner.nextInt();
            if (enteredRecieverId == activeCustomer.getId()) {
                incorrectInput("You can not enter your own id");
                payment();
            }

            try {
                if (customerService.userExists(enteredRecieverId)) {
                    reciever = customerService.getCustomer(enteredRecieverId);
                }else{
                    throw new RecordNotFoundException("No user with such ID");
                }
            } catch (RecordNotFoundException e) {
                incorrectInput(e.getMessage());
                payment();
            }

        } catch (Exception e) {
            scanner.nextLine();
            incorrectInput("Incorrectly selected receiver's ID!");
            payment();
        }


        System.out.println("Enter the amount you want to send: ");

        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            scanner.nextLine();
            incorrectInput("Invalid amount!");
            payment();
        }

        if (amount > 0) {
            try {
                customerService.transfer(activeCustomer, reciever, amount);
                customerService.registerPayment(activeCustomer, reciever, amount, getTime("HH:mm"), getTime("dd.MM.yyyy"));
                System.out.println("Payment has been successful!");
                customerChoices();
            } catch (RecordNotFoundException e) {
                System.err.println("Daco zle");
                customerChoices();
            } catch (NotEnoughMoneyException e) {
                incorrectInput("Not enough money on your account");
                payment();
            }
        } else {
            incorrectInput("Please select a valid amount!");
            payment();
        }
    }


    public void showHistoryOfPayments() {
        int choice;

        System.out.println(
                "________________________________________\n"
                        + "|Enter the choice: \n"
                        + "|History of sent payments: (press 1)\n"
                        + "|History of received payments: (press 2)");
        try {
            choice = scanner.nextInt();

            try {
                paymentsPrinter(PaymentHistoryType.fromValue(choice));
            } catch (InputErrorException e) {
                System.err.println(e);
                scanner.nextLine();
                incorrectInput("Invalid input");
                showHistoryOfPayments();
            }
        } catch (Exception e) {
            System.err.println(e);
            scanner.nextLine();
            incorrectInput("Invalid input");
            showHistoryOfPayments();
        }
    }

    public void accountInfo() throws RecordNotFoundException {
        System.out.println(
                "______________________________\n"
                        + "|Your name: " + activeCustomer.getFirstName() + ' ' + activeCustomer.getLastName() + "\n"
                        + "|Your email: " + activeCustomer.getEmail() + "\n"
                        + "|Your id: " + activeCustomer.getId() + "\n"
                        + "|Your balance: " + customerService.getCurrentBalance(activeCustomer)
        );
        customerChoices();
    }

    public void passwordChange() {
        String oldPassword, newPassword;

        scanner.nextLine();

        System.out.println("Enter your old password: ");
        oldPassword = scanner.nextLine();

        if (getHash(oldPassword).equals(activeCustomer.getPassword())) {
            System.out.println("Enter your new password: ");
            newPassword = scanner.nextLine();

            if (newPassword.length() >= 8) {
                String newPasswordHash = getHash(newPassword);
                try {
                    customerService.changePassword(activeCustomer, newPasswordHash);
                    System.out.println("Password has been changed! You are logged out.");
                    activeCustomer = null;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    defaultChoice();
                }
            } else {
                incorrectInput("Password must have at least 8 characters");
                customerChoices();
            }
        } else {
            incorrectInput("Your password is incorrect");
            passwordChange();
        }
    }

    /**
     * Prints the list of payments
     *
     * @param paymentHistoryType    sent or received payments
     */

    public void paymentsPrinter(PaymentHistoryType paymentHistoryType){
        List<Payment> payments = customerService.getListOfPayments(activeCustomer, paymentHistoryType);
        if (payments.size() > 0) {
            for (Payment payment : payments) {
                System.out.println(
                        "________________________________________\n"
                                + "|Sender's id: " + payment.getSenderId() + "\n"
                                + "|Sender's name: " + payment.getSender() + "\n"
                                + "|Receiver's id: " + payment.getRecieverId() + "\n"
                                + "|Receiver's name: " + payment.getReceiver() + "\n"
                                + "|Sent amount: " + payment.getAmount() + "\n"
                                + "|Payment time: " + payment.getTime() + "\n"
                );
            }
        } else {
            System.out.println("No payments yet");
        }
        customerChoices();
    }

    /**
     * Allows user to decide, where to continue if incorrect input is entered
     *
     * @param error     error message
     */

    public void incorrectInput(String error) {
        System.err.println(error);

        System.out.println(
                "Try again (press 1)\n"
                        + "Exit (press 2)");

        int choice;
        try {
            choice = scanner.nextInt();

            if (choice == 1) {
                //continues
            } else if (choice == 2) {
                customerChoices();
            } else {
                incorrectInput("Incorrect choice!!");
            }

        } catch (Exception e) {
            scanner.nextLine();
            incorrectInput("Incorrect choice!");
        }
    }

    /**
     * Get current time of action
     *
     * @param pattern   time format you want to receive
     * @return          current time
     */

    public String getTime(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    /**
     * Getting input from user
     *
     * @param requiredField     type of data we ask for
     * @return                  user input
     */
    private String askUserToEnterValue(String requiredField) {
        System.out.println(
                "________________________________________\n"
                        + "|Enter your " + requiredField + ": \n"
        );
        return scanner.nextLine();
    }

}
