package com.poc.example.contactsdetails;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * SyncService.
 */

public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter mSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.i("INFO", "Sync Service created.");
        synchronized (sSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("INFO", "Sync Service binded.");
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
