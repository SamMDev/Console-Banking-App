package com.sammdev.db.model;

public class Payment {
    private String sender, receiver, time;
    private int senderId, recieverId;
    private double amount;

    public Payment(int senderId, String sender, int recieverId, String receiver, double amount, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.amount = amount;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getTime() {
        return time;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getRecieverId() {
        return recieverId;
    }

    public double getAmount() {
        return amount;
    }
}
