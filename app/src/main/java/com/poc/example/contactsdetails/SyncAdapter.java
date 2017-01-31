package com.poc.example.contactsdetails;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * SyncAdapter.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i("INFO", "Sync Adapter created.");
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i("INFO", "Sync Adapter called.");
    }
}
