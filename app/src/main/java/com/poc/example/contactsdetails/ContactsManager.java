package com.poc.example.contactsdetails;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactsManager
 */

public class ContactsManager {

    private static String MIMETYPE = "vnd.android.cursor.item/com.poc.example.contactsdetails";

    public static void addContact(Context context, MyContact contact) {

        ContentResolver resolver = context.getContentResolver();
        boolean mHasAccount = isAlreadyRegistered(resolver, contact.getId());

        if (mHasAccount) {
            Log.i("ContactsManager", context.getString(R.string.account_exists) + contact.getName());
        } else {
            Log.i("ContactsManager", "New Account, " + contact.getName() + "    Number = " + contact.getNumber());
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            // insert account name and account type
            ops.add(ContentProviderOperation
                    .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.RawContacts.CONTENT_URI, true))
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, Constants.ACCOUNT_NAME)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
                    .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                            ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                    .build());

            // Insert contact number
            ops.add(ContentProviderOperation
                    .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getNumber())
                    .build());

            // Insert contact name
            ops.add(ContentProviderOperation
                    .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                    .build());

            // Insert mime-type data
            ops.add(ContentProviderOperation
                    .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE)
                    .withValue(ContactsContract.Data.DATA1, 12345)
                    .withValue(ContactsContract.Data.DATA2, "user")
                    .withValue(ContactsContract.Data.DATA3, "MyApplication")
                    .build());

            try {
                resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Check if contact is already registered with app
     *
     * @param resolver
     * @param Ids
     * @return
     */
    private static boolean isAlreadyRegistered(ContentResolver resolver, List<String> Ids) {
        boolean isRegistered = false;
        List<String> str = new ArrayList<>();
        try {
            // For Each Id find if ContactDetails is Exist.
            for (String id : Ids) {

                //query raw contact id's from the contact id
                Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts._ID},
                        ContactsContract.RawContacts.CONTACT_ID + "=?",
                        new String[]{id}, null);

                //fetch all raw contact id's and save them in a list of string
                if (c != null && c.moveToFirst()) {
                    do {
                        str.add(c.getString(c.getColumnIndexOrThrow(ContactsContract.RawContacts._ID)));
                    } while (c.moveToNext());
                    c.close();
                } else {
                    c.close();
                }


                //query account types and check the account type for each raw contact id
                for (int i = 0; i < str.size(); i++) {
                    Cursor c1 = resolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts.ACCOUNT_TYPE},
                            ContactsContract.RawContacts._ID + "=?",
                            new String[]{str.get(i)}, null);

                    if (c1 != null) {
                        c1.moveToFirst();
                        String accType = c1.getString(c1.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE));
                        if (accType != null && accType.equals(Constants.ACCOUNT_TYPE)) {
                            isRegistered = true;
                            break;
                        }
                        c1.close();
                    } else {
                        c1.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isRegistered;
    }

    /**
     * Check for sync call
     *
     * @param uri
     * @param isSyncOperation
     * @return
     */
    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
        return uri;
    }

}