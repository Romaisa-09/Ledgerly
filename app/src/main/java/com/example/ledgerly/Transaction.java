package com.example.ledgerly;

public class Transaction {
    private int vendorId;
    private int customerId;
    private int transactionId;
    private String name;
    private String date;
    private String time;
    private int amount;
    private int sendFlag;
    private int receiveFlag;

    public Transaction() { }

    public Transaction(int vendorId,
                       int customerId,
                       int transactionId,
                       String name,
                       String date,
                       String time,
                       int amount,
                       int sendFlag,
                       int receiveFlag) {
        this.vendorId      = vendorId;
        this.customerId    = customerId;
        this.transactionId = transactionId;
        this.name          = name;
        this.date          = date;
        this.time          = time;
        this.amount        = amount;
        this.sendFlag      = sendFlag;
        this.receiveFlag   = receiveFlag;
    }

    public int getVendorId() {
        return vendorId;
    }
    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getCustomerId() {
        return customerId;
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getSendFlag() {
        return sendFlag;
    }
    public void setSendFlag(int sendFlag) {
        this.sendFlag = sendFlag;
    }

    public int getReceiveFlag() {
        return receiveFlag;
    }
    public void setReceiveFlag(int receiveFlag) {
        this.receiveFlag = receiveFlag;
    }
}
