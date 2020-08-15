package com.sammdev.db.dao;

import com.sammdev.exception.FailedToCreateRecordException;
import com.sammdev.exception.FailedToLoadResultException;
import com.sammdev.exception.FailedToUpdateRecordException;
import com.sammdev.exception.RecordNotFoundException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.sammdev.db.config.DbConfig.DB_PASSWORD;
import static com.sammdev.db.config.DbConfig.DB_URL;
import static com.sammdev.db.config.DbConfig.DB_USER;

public class BalanceDao {

    private static final BalanceDao instance = new BalanceDao();
    private Connection connection;

    private BalanceDao() {
        // hide constructor, singleton patter
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static BalanceDao getInstance() {
        return instance;
    }

    /**
     * Creates a record in balance table
     *
     * @param id     id of the row
     * @param amount initial balance
     * @throws FailedToCreateRecordException if creation fails
     */
    public void createBalance(int id, double amount) throws FailedToCreateRecordException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO balance VALUES (?,?)")) {
            statement.setInt(1, id);
            statement.setDouble(2, amount);
            statement.execute();
        } catch (SQLException e) {
            throw new FailedToCreateRecordException("Failed to create balance record!");
        }
    }

    /**
     * Updates a record in a balance table
     *
     * @param id     id of the record
     * @param amount new amount
     * @throws FailedToUpdateRecordException
     */
    public void updateBalance(int id, double amount) throws FailedToUpdateRecordException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE balance SET balance=? where id=?")) {
            statement.setDouble(1, amount);
            statement.setInt(2, id);
            statement.execute();
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToUpdateRecordException("Failed to update balance record!");
        }
    }

    /**
     * Gets a balance in the balance table
     *
     * @param id id of the record
     * @return balance amount
     * @throws RecordNotFoundException     if the record does not exist
     * @throws FailedToLoadResultException if the exception occurred during reading db
     */
    public Double getBalance(int id) throws RecordNotFoundException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT balance FROM balance WHERE id=?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            } else {
                throw new RecordNotFoundException("Balance record with id " + id + " does not exist!");
            }
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToLoadResultException("Failed to read balance record!");
        }

    }

}
