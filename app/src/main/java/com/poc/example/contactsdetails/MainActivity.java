package com.poc.example.contactsdetails;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mNumbers = new ArrayList<>();
    private Map<String, List<String>> idsHash = new HashMap<>();
    // ListView Reference.
    private ListView lvContactsNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the list view
        this.lvContactsNames = (ListView) findViewById(R.id.lvContactsNames);


        // Read and show the contacts
        showContacts();
    }

    /**
     * Show the contacts in the ListView.
     */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            getContactDataBefore();

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNames);
            lvContactsNames.setAdapter(adapter);
            lvContactsNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getBaseContext(),ViewingActivity.class);
                    intent.putExtra("name", (String) parent.getItemAtPosition(position));
                    startActivity(intent);
                }
            });

            applyChanges();

            // Set Android Account work Automatically
            setAccount();
        }
    }

    /**
     * Set Android Account work Automatically without manual operation.
     */
    private void setAccount() {
        AccountManager accountManager = AccountManager.get(this); //this is Activity
        Account account = new Account("MyAccount", "com.poc.example.contactsdetails");
        boolean success = accountManager.addAccountExplicitly(account, null, null);

        if (success) {
            Log.d("DEBUG", "Account created!!");
        } else {
            Log.d("DEBUG", "Account creation failed. Look at previous logs to investigate");
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

    /**
     * Apply changes to phone's contact list
     */
    private void applyChanges() {

        Intent cIntent = new Intent(this, ContactService.class);
        cIntent.putExtra("ids",mIDs);
        cIntent.putExtra("numbers",mNumbers);
        cIntent.putExtra("names",mNames);
        cIntent.putExtra("hash", (Serializable) idsHash);
        startService(cIntent);

    }

    /**
     * Get The Permission Result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we can't display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
