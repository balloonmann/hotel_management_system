package com.hotel.models;

public class Admin extends Person {
    private String staffId;

    public Admin(String staffId, String name, String contactNumber) {
        super(name, contactNumber);
        this.staffId = staffId;
    }

    public String getStaffId() {
        return staffId;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
