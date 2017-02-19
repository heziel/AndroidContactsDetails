package com.poc.example.contactsdetails;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ContactService.
 */

public class ContactService extends IntentService {
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mNumbers = new ArrayList<>();
    private Map<String, List<String>> idsHash = new HashMap<>();

    public ContactService() {
        super("ContactService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIDs = intent.getStringArrayListExtra("ids");
        mNames = intent.getStringArrayListExtra("names");
        mNumbers = intent.getStringArrayListExtra("numbers");
        idsHash = (Map<String, List<String>>) intent.getSerializableExtra("hash");

        String name, number, id;
        for (int i = 0; i < mIDs.size(); i++) {
            id = mIDs.get(i);
            number = mNumbers.get(i);
            name = mNames.get(i);
            ContactsManager.addContact(getApplication(),new MyContact(idsHash.get(name), number, name));
        }

    }
}
