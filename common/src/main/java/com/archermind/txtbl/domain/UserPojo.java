package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserPojo implements Serializable {

    private static final long serialVersionUID = 1L;

    List<Contact> contact = new ArrayList<Contact>();
    List<Account> account = new ArrayList<Account>();
    Device device = new Device();
    BillHistory billHistory = new BillHistory();
    User user = new User();

    public List<Account> getAccount() {
        return account;
    }

    public void setAccount(List<Account> account) {
        this.account = account;
    }

    public BillHistory getBillHistory() {
        return billHistory;
    }

    public void setBillHistory(BillHistory billHistory) {
        this.billHistory = billHistory;
    }

    public List<Contact> getContact() {
        return contact;
    }

    public void setContact(List<Contact> contact) {
        this.contact = contact;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
