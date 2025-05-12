package com.example.ledgerly;

public class Customer {
    private int cid;
    private int vid;
    private String name;
    private String date;
    private String time;
    private int remainingAmount;
    private String phoneNumber;

    public Customer() { }

    public Customer(int cid, int vid, String name, String date,
                    String time, int remainingAmount, String phoneNumber) {
        this.cid = cid;
        this.vid = vid;
        this.name = name;
        this.date = date;
        this.time = time;
        this.remainingAmount = remainingAmount;
        this.phoneNumber = phoneNumber;
    }

    public int getCid() { return cid; }
    public void setCid(int cid) { this.cid = cid; }

    public int getVid() { return vid; }
    public void setVid(int vid) { this.vid = vid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(int remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
