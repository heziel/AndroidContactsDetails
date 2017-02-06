package com.poc.example.contactsdetails;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewingActivity extends AppCompatActivity {
    private Context mContext;
    TextView tvName;
    TextView tvNumber;
    private String name = "";
    private String number = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);

        mContext = getBaseContext();

        tvName = (TextView) findViewById(R.id.tvName);
        tvNumber = (TextView) findViewById(R.id.tvNumber);

        getContactDetails();

        tvName.setText(name);
        tvNumber.setText(number);
    }

    private void getContactDetails() {
        Intent intent = getIntent();

        Uri uri = intent.getData();
        String key = getKey(uri);
        getContactNameFromAndroidKey(key);
    }

    /*
    *  Get contact key.
    */
    private String getKey(Uri uriData){

        Cursor c = this.managedQuery(uriData,null,null,null,null);
        c.moveToFirst();
        String lookupKey = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY ));
        c.close();
        return lookupKey;
    }


    /*
    *   Get Contact Name From Android Key.
    */
    private void getContactNameFromAndroidKey (String key)
    {
        // GET Name

        // Run query
        Uri uri = Uri.parse (ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + key);

        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,

        };

        Cursor cursor = mContext.getContentResolver().query (
                uri,
                projection,
                null,
                null,
                null);

        if (!cursor.moveToNext()) // move to first (and only) row.
            throw new IllegalStateException ("contact no longer exists for key");
        String id = cursor.getString(0);
        name = cursor.getString(1);

        cursor.close();

        // GET Number
        cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "+ id,
                null, null);
        if (cursor.moveToFirst()) {
            int colIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            number = cursor.getString(colIdx);
        }
        cursor.close();
    }
}
