package app.mynote.core.db;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class NoteSyncService extends Service {
    /**
     * Lock use to synchronize instantiation of SyncAdapter.
     */
    private static final Object LOCK = new Object();
    private static NoteSyncAdapter syncAdapter;


    @Override
    public void onCreate() {
        // SyncAdapter is not Thread-safe
        synchronized (LOCK) {
            // Instantiate our SyncAdapter
            syncAdapter = new NoteSyncAdapter(this, false);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return our SyncAdapter's IBinder
        return syncAdapter.getSyncAdapterBinder();
    }
}
