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
    private  Cursor cursorName;
    private Cursor cursorNumber;

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
        if ( uri != null) {
            String key = getKey(uri);
            getContactNameFromAndroidKey(key);
        }else{
            name = intent.getStringExtra("name");
        }

    }

    /*
    *  Get contact key.
    */
    private String getKey(Uri uriData){

        Cursor c = getContentResolver().query(uriData,null,null,null,null);
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

        cursorName = mContext.getContentResolver().query (
                uri,
                projection,
                null,
                null,
                null);

        if (!cursorName.moveToNext()) // move to first (and only) row.
            throw new IllegalStateException ("contact no longer exists for key");
        String id = cursorName.getString(0);
        name = cursorName.getString(1);

        cursorName.close();

        // GET Number
        cursorNumber = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "+ id,
                null, null);
        if (cursorNumber.moveToFirst()) {
            int colIdx = cursorNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            number = cursorNumber.getString(colIdx);
        }
        cursorNumber.close();
    }
}
