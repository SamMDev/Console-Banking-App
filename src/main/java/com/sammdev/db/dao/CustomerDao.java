package com.sammdev.db.dao;

import com.sammdev.db.model.Customer;
import com.sammdev.exception.FailedToCreateRecordException;
import com.sammdev.exception.FailedToLoadResultException;
import com.sammdev.exception.FailedToUpdateRecordException;
import com.sammdev.exception.RecordNotFoundException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.sammdev.db.config.DbConfig.DB_PASSWORD;
import static com.sammdev.db.config.DbConfig.DB_URL;
import static com.sammdev.db.config.DbConfig.DB_USER;

public class CustomerDao {

    private static final CustomerDao instance = new CustomerDao();

    private Connection connection;

    private CustomerDao() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static CustomerDao getInstance() {
        return instance;
    }


    /**
     * Gets id of last customer registered in database
     *
     * @return  id of last registered customer
     */
    public int getLastId() {
        int returnValue = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select max(id) from customers");
            resultSet.next();
            returnValue = resultSet.getInt(1);
            statement.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return returnValue;
    }


    /**
     * Creates new entry of the customer
     *
     * @param newCustomer customer representation
     * @throws FailedToCreateRecordException if an error occurs
     */
    public void createCustomer(Customer newCustomer) throws FailedToCreateRecordException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO customers VALUES(?,?,?,?,?,?)")) {
            statement.setInt(1, newCustomer.getId());
            statement.setString(2, newCustomer.getFirstName());
            statement.setString(3, newCustomer.getLastName());
            statement.setString(4, newCustomer.getAddress());
            statement.setString(5, newCustomer.getEmail());
            statement.setString(6, newCustomer.getPassword());
            statement.execute();
        } catch (SQLException e) {
            System.out.println(e);
            throw new FailedToCreateRecordException("Failed to create customer!");
        }
    }


    /**
     * Gets user by his email
     *
     * @param email user email
     * @return Customer object
     * @throws RecordNotFoundException if a user was not found
     */
    public Customer findCustomer(String email) throws RecordNotFoundException {

        try (PreparedStatement statement = connection.prepareStatement("SELECT * from customers where email = ?")) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Customer(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6)
                );
            } else {
                throw new RecordNotFoundException("Unable to find user with given email!");
            }
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToLoadResultException("Failed to load user!"); //runtime exception
        }
    }

    /**
     * Gets user by his id
     *
     * @param id user id
     * @return Customer object
     * @throws RecordNotFoundException if a user was not found
     */
    public Customer findCustomer(int id) throws RecordNotFoundException {

        try (PreparedStatement statement = connection.prepareStatement("SELECT * from customers where id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Customer(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6)
                );
            } else {
                throw new RecordNotFoundException("Unable to find user with given ID!");
            }
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToLoadResultException("Failed to load user!"); //runtime exception
        }
    }

    /**
     * Updates customer info
     *
     * @param customer  customer with new actual info
     */
    public void updateCustomer(Customer customer) {
        try (PreparedStatement statement =
                     connection.prepareStatement("UPDATE customers SET first_name=?, last_name=?, email=?, address=?, password=? WHERE id = ?")) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getAddress());
            statement.setString(5, customer.getPassword());
            statement.setInt(6, customer.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToUpdateRecordException("Failed to update user!");
        }
    }

}
