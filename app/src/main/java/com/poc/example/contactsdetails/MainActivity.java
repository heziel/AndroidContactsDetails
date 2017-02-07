package com.poc.example.contactsdetails;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
            applyChanges();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNames);
            lvContactsNames.setAdapter(adapter);

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

                    if ( idsHash.containsKey(name) ) {
                        list = idsHash.get(name);
                    }else{
                        mIDs.add(id);
                        mNames.add(name);
                        mNumbers.add(c2.getString(c2
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }

                    list.add(id);
                    idsHash.put(name,list);

                    // add contact number's to the mNumbers list
                    //do{
                   // mNumbers.add(c2.getString(c2
                    //        .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    //}while (c2.moveToNext());
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

        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                String name, number, id;
                for (int i = 0; i < mIDs.size(); i++) {
                    id = mIDs.get(i);
                    number = mNumbers.get(i);
                    name = mNames.get(i);
                   // Log.d("CONTACTS", "Name=" + name + " Number =" + number);
                    ContactsManager.addContact(MainActivity.this,new MyContact(idsHash.get(name), number, name));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
            }
        }.execute();
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
