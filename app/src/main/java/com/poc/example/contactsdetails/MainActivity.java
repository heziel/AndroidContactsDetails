package com.poc.example.contactsdetails;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    // ListView Reference.
    private ListView lvContactsNames;
    ContactsResultReceiver resultReceiver;

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
     * Apply changes to phone's contact list
     */
    private void applyChanges() {
        resultReceiver = new ContactsResultReceiver(new Handler());

        Intent cIntent = new Intent(this, ContactService.class);
        cIntent.putExtra("receiver", resultReceiver);
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


    private class ContactsResultReceiver extends ResultReceiver {

        public static final int ERROR = 0;
        public static final int SUCCESS = 1;

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public ContactsResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            ArrayList<String> mNames = new ArrayList<>();
            mNames = resultData.getStringArrayList("names");

            switch (resultCode) {
                case SUCCESS:
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(),
                            android.R.layout.simple_list_item_1, mNames);
                    lvContactsNames.setAdapter(adapter);
                    lvContactsNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getBaseContext(), ViewingActivity.class);
                            intent.putExtra("name", (String) parent.getItemAtPosition(position));
                            startActivity(intent);
                        }
                    });
                    break;
                case ERROR:
                    Toast.makeText(getApplication(), "Contact List Is Empty", Toast.LENGTH_SHORT).show();

            }
            super.onReceiveResult(resultCode, resultData);
        }
    }
}