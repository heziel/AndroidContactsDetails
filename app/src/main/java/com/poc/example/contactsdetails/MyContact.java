package com.poc.example.contactsdetails;

import java.util.ArrayList;
import java.util.List;

/**
 * MyContact.
 */
public class MyContact {

    public String name;
    public String number;
    public static List<String> mNumbersList = new ArrayList<>();
    public String Id;

    public MyContact(String Id, String number, String name) {
        this.name = name;
        this.Id = Id;
        this.number = number;
        mNumbersList.add(number);
    }
}
