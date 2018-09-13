package ru.com.penza.myfinalapp.datamodel;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Contact {

    @SerializedName("first_name")
    @Expose
    protected String firstName;
    @SerializedName("last_name")
    @Expose
    protected String lastName;
    @SerializedName("second_name")
    @Expose
    protected String secondName;
    @SerializedName("phone")
    @Expose
    protected String phone;
    @SerializedName("color")
    @Expose
    protected String color="#FFFFFF";

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}