package com.poc.example.contactsdetails;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.os.ResultReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ContactService.
 */

public class ContactService extends IntentService {
    public static final int ERROR = 0;
    public static final int SUCCESS = 1;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mNumbers = new ArrayList<>();
    private Map<String, List<String>> idsHash = new HashMap<>();

    public ContactService() {
        super("ContactService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();

        getContactDataBefore();

        // send the result back to the Main Thread.
        bundle.putStringArrayList("names", mNames);

        if (mNames != null)
            receiver.send(SUCCESS, bundle);
        else
            receiver.send(ERROR, bundle.EMPTY);

        String name, number, id;
        for (int i = 0; i < mIDs.size(); i++) {
            id = mIDs.get(i);
            number = mNumbers.get(i);
            name = mNames.get(i);
            ContactsManager.addContact(getApplication(), new MyContact(idsHash.get(name), number, name));
        }
    }

    /**
     * Method to fetch contact's from device
     */
    private void getContactDataBefore() {
        int i = 0;
        List<String> list = new ArrayList<>();

        Cursor c1 = getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if ((c1 != null) && c1.moveToFirst()) {

            // add contact id's to the mIDs list
            do {
                String id = c1.getString(c1.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = c1.getString(c1.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                // query all contact numbers corresponding to current id
                Cursor c2 = getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                new String[]{id}, null);

                if (c2 != null && c2.moveToFirst()) {
                    //  Log.d("DEBUG","name =" + name);
                    list = new ArrayList<>();

                    if (idsHash.containsKey(name)) {
                        list = idsHash.get(name);
                    } else {
                        mIDs.add(id);
                        mNames.add(name);
                        mNumbers.add(c2.getString(c2
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }

                    list.add(id);
                    idsHash.put(name, list);

                    c2.close();
                } else {
                    c2.close();
                }

                i++;
            } while (c1.moveToNext() && i < c1.getCount());

            c1.close();
        }
    }
}