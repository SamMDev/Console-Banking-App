package com.sammdev.db.dao;


import com.sammdev.db.model.Payment;
import com.sammdev.exception.FailedToCreateRecordException;
import com.sammdev.exception.FailedToLoadResultException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.sammdev.db.config.DbConfig.DB_PASSWORD;
import static com.sammdev.db.config.DbConfig.DB_URL;
import static com.sammdev.db.config.DbConfig.DB_USER;

public class PaymentDao {
    private static PaymentDao instance = new PaymentDao();
    private Connection connection;

    private static final String PAYMENTS_QUERY =
            "select " +
            "p.sender_id," +
            "concat(customer_sender.first_name, ' ', customer_sender.last_name) as sender_name," +
            "p.reciever_id," +
            "concat(customer_receiver.first_name, ' ', customer_receiver.last_name) as receiver_name," +
            "p.amount," +
            "concat(p.date, ' ', p.time) as time " +
            "from payments p " +
            "join customers customer_sender on customer_sender.id = p.sender_id " +
            "join customers customer_receiver on customer_receiver.id = p.reciever_id ";


    private PaymentDao() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static PaymentDao getInstance() {
        return instance;
    }


    /**
     * Gets id of last registered payment
     * @return  id of last registered payment
     */
    public int getLastId() {
        int returnValue = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from payments");
            resultSet.next();
            returnValue = resultSet.getInt(1);
            statement.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return returnValue;
    }


    /**
     * Gets list of received payments
     *
     * @param receiverId    id of receiver
     * @return              List of sent payments
     */
    public List<Payment> getReceivedPayments(int receiverId) {
        List<Payment> paymentsList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String query = PAYMENTS_QUERY + " where reciever_id = " + receiverId;

            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                paymentsList.add(
                        new Payment(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getInt(3),
                                resultSet.getString(4),
                                resultSet.getDouble(5),
                                resultSet.getString(6)
                        )
                );
            }

            return paymentsList;
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToLoadResultException("Failed to load list of payments");
        }
    }

    /**
     * Gets list of sent payments
     *
     * @param senderId  id of sender
     * @return          List of received payments
     */
    public List<Payment> getSentPayments(int senderId){
        List<Payment> paymentsList = new ArrayList<>();
        try(Statement statement = connection.createStatement()){
            String query = PAYMENTS_QUERY + " where sender_id = " + senderId;

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                paymentsList.add(
                        new Payment(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getInt(3),
                                resultSet.getString(4),
                                resultSet.getDouble(5),
                                resultSet.getString(6)
                        )
                );
            }
            return paymentsList;
        }catch (SQLException e){
            System.err.println(e);
            throw new FailedToLoadResultException("Failed to load list of payments");
        }
    }


    /**
     * Registers payment into database
     *
     * @param senderId
     * @param receiverId
     * @param amount
     * @param time
     * @param date
     * @throws FailedToCreateRecordException    If an error occurs
     */
    public void createPayment(int senderId, int receiverId, double amount, String time, String date) throws FailedToCreateRecordException {
        String query = "insert into payments values (?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, getLastId() + 1);
            statement.setInt(2, senderId);
            statement.setInt(3, receiverId);
            statement.setDouble(4, amount);
            statement.setString(5, time);
            statement.setString(6, date);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e);
            throw new FailedToCreateRecordException("Failed to create payment record");
        }
    }
}
