package com.poc.example.contactsdetails;

import java.util.ArrayList;
import java.util.List;

/**
 * MyContact.
 */
public class MyContact {
    private List<String> Id;
    private String name;
    private String number;
    private static List<String> mNumbersList = new ArrayList<>();

    public MyContact(List<String> Id, String number, String name) {
        this.name = name;
        this.Id = Id;
        this.number = number;
        mNumbersList.add(number);
    }

    public List<String> getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
