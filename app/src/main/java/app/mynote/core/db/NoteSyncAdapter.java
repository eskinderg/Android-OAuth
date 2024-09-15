package app.mynote.core.db;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.mynote.auth.AuthConfig;
import app.mynote.core.callback.AppCallback;
import app.mynote.core.db.auth.AccountGeneral;
import app.mynote.core.utils.AppTimestamp;
import app.mynote.fragments.note.Note;
import app.mynote.fragments.note.NotesDataService;
import app.mynote.service.RetroInstance;
import retrofit2.Call;
import retrofit2.Retrofit;

public class NoteSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC_ADAPTER";

    private final ContentResolver resolver;


    public NoteSyncAdapter(Context c, boolean autoInit) {
        this(c, autoInit, false);
    }

    public NoteSyncAdapter(Context c, boolean autoInit, boolean parallelSync) {
        super(c, autoInit, parallelSync);
        this.resolver = c.getContentResolver();
    }

    /**
     * Manual force Android to perform a sync with our SyncAdapter.
     */
    public static void performSync() {
        // First cancel any ongoing sync.
        cancelSync();
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(),
                NoteContract.CONTENT_AUTHORITY, b);
    }

    public static void cancelSync() {
        ContentResolver.cancelSync(AccountGeneral.getAccount(), NoteContract.CONTENT_AUTHORITY);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        if( AuthConfig.USER != null) {

            Log.i(TAG, "Starting synchronization...");

            try {
                syncNotes(syncResult);
                // Add any other things you may want to sync

            } catch (IOException ex) {
                Log.e(TAG, "Error synchronizing!", ex);
                syncResult.stats.numIoExceptions++;
            } catch (JSONException ex) {
                Log.e(TAG, "Error synchronizing!", ex);
                syncResult.stats.numParseExceptions++;
            } catch (RemoteException | OperationApplicationException ex) {
                Log.e(TAG, "Error synchronizing!", ex);
                syncResult.stats.numAuthExceptions++;
            }

        }
    }

    private void syncNotes(SyncResult syncResult) throws IOException, JSONException, RemoteException, OperationApplicationException {
        Map<String, Note> localEntries = new HashMap<>();
        Map<String, Note> pushEntries = new HashMap<>();

        // We need to collect all the network items in a hash table
        Log.i(TAG, "Fetching server entries...");

        Retrofit retrofit = RetroInstance.getRetrofitInstance();
        NotesDataService notesApi = retrofit.create(NotesDataService.class);
        Call<Note[]> call = notesApi.getNotes();
        call.enqueue(new AppCallback<Note[]>(getContext()) {
            @Override
            public void onResponse(Note[] response) {

                try {
                    Note found;
                    Map<String, Note> networkEntries = new HashMap<>();

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    // Compare the hash table of network entries to all the local entries
                    Log.i(TAG, "Fetching local entries...");
                    Cursor c = resolver.query(NoteContract.Notes.CONTENT_URI, null, NoteContract.Notes.COL_USER_ID + " = '" + AuthConfig.USER.getId() + "'", null, null, null);
                    assert c != null;

                    for (Note remoteNote : response) {
                        networkEntries.put(remoteNote.getId() + remoteNote.getUserId(), remoteNote);
                    }

                    c.moveToFirst();

                    for (int i = 0; i < c.getCount(); i++) {
                        syncResult.stats.numEntries++;
                        Note noteLocal = new Note();
                        noteLocal.setId(c.getString(c.getColumnIndex(NoteContract.Notes.COL_ID)));
                        noteLocal.setHeader(c.getString(c.getColumnIndex(NoteContract.Notes.COL_HEADER)));
                        noteLocal.setText(c.getString(c.getColumnIndex(NoteContract.Notes.COL_TEXT)));
                        noteLocal.setUserId(c.getString(c.getColumnIndex(NoteContract.Notes.COL_USER_ID)));
                        noteLocal.setSelection(c.getString(c.getColumnIndex(NoteContract.Notes.COL_SELECTION)));
                        noteLocal.setColour(c.getString(c.getColumnIndex(NoteContract.Notes.COL_COLOUR)));
                        noteLocal.setArchived(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_ARCHIVED)) > 0);
                        noteLocal.setPinned(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_PINNED)) > 0);
                        noteLocal.setActive(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_ACTIVE)) > 0);
                        noteLocal.setSpellCheck(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_SPELL_CHECK)) > 0);
                        noteLocal.setPinOrder(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(NoteContract.Notes.COL_PIN_ORDER))));
                        noteLocal.setDateCreated(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_CREATED))));
                        noteLocal.setDateModified(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_MODIFIED))));
                        noteLocal.setDateArchived(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_ARCHIVED))));
                        noteLocal.setDateSync(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_SYNC)));
                        noteLocal.setOwner(c.getString(c.getColumnIndex(NoteContract.Notes.COL_OWNER)));

                        // Try to retrieve the local entry from network entries
                        found = networkEntries.get(noteLocal.getId() + noteLocal.getUserId());

                        archives(localEntries, batch, noteLocal, found);
                        pinOrder(localEntries, batch, noteLocal, found);

                        if (found != null) {
                            int des = found.getDateModified().toString().compareTo(noteLocal.getDateModified().toString());

                            if (des > 0) {
                                batch.add(ContentProviderOperation.newUpdate(NoteContract.Notes.CONTENT_URI)
                                        .withSelection(NoteContract.Notes.COL_ID + "='" + found.getId() + "'", null)
                                        .withValue(NoteContract.Notes.COL_ID, found.getId())
                                        .withValue(NoteContract.Notes.COL_HEADER, found.getHeader())
                                        .withValue(NoteContract.Notes.COL_TEXT, found.getText())
                                        .withValue(NoteContract.Notes.COL_USER_ID, found.getUserId())
                                        .withValue(NoteContract.Notes.COL_COLOUR, found.getColour())
                                        .withValue(NoteContract.Notes.COL_SELECTION, found.getSelection())
                                        .withValue(NoteContract.Notes.COL_ARCHIVED, found.getArchived())
                                        .withValue(NoteContract.Notes.COL_PINNED, found.getPinned())
                                        .withValue(NoteContract.Notes.COL_ACTIVE, found.getActive())
                                        .withValue(NoteContract.Notes.COL_SPELL_CHECK, found.getSpellCheck())
                                        .withValue(NoteContract.Notes.COL_PIN_ORDER, found.getPinOrder().toString())
                                        .withValue(NoteContract.Notes.COL_DATE_CREATED, found.getDateCreated().toString())
                                        .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, found.getDateArchived().toString())
                                        .withValue(NoteContract.Notes.COL_DATE_MODIFIED, found.getDateModified().toString())
                                        .withValue(NoteContract.Notes.COL_DATE_SYNC, found.getDateSync())
                                        .withValue(NoteContract.Notes.COL_OWNER, found.getOwner())
                                        .build());

                            } else if (des < 0) {
                                Log.i("SERVER", found.getHeader() + "Needs to be sent to the server");
                                localEntries.put(noteLocal.getId() + noteLocal.getUserId(), noteLocal);
                            } else {
//                                Log.w("Eskinder", found.getHeader() + "=0");
                            }

                            networkEntries.remove(noteLocal.getId() + noteLocal.getUserId());
                        } else {
                            // new items found on local
                            pushEntries.put(noteLocal.getId() + noteLocal.getUserId(), noteLocal);
                        }


                        c.moveToNext();
                    }
                    c.close();

                    if (pushEntries.size() > 0) {
                        Retrofit localToRemoteRetrofit = RetroInstance.getRetrofitInstance();
                        NotesDataService localToRemoteNotesApi = localToRemoteRetrofit.create(NotesDataService.class);
                        Call<Note[]> callLocalToRemote = localToRemoteNotesApi.insert(new ArrayList<>(pushEntries.values()));
                        callLocalToRemote.enqueue(new AppCallback<Note[]>(getContext()) {
                            @Override
                            public void onResponse(Note[] response) {
                                pushEntries.clear();
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.e(TAG, "Error pushing local changes " + throwable.getMessage());
                            }
                        });
                    }
                    pushEntries.clear();

                    // Add all the new entries
                    for (Note note : networkEntries.values()) {
                        Log.i(TAG, "Scheduling insert: " + note.getHeader());
                        batch.add(ContentProviderOperation.newInsert(NoteContract.Notes.CONTENT_URI)
                                .withValue(NoteContract.Notes.COL_ID, note.getId())
                                .withValue(NoteContract.Notes.COL_HEADER, note.getHeader())
                                .withValue(NoteContract.Notes.COL_TEXT, note.getText())
                                .withValue(NoteContract.Notes.COL_USER_ID, note.getUserId())
                                .withValue(NoteContract.Notes.COL_COLOUR, note.getColour())
                                .withValue(NoteContract.Notes.COL_SELECTION, note.getSelection())
                                .withValue(NoteContract.Notes.COL_ARCHIVED, note.getArchived())
                                .withValue(NoteContract.Notes.COL_PINNED, note.getPinned())
                                .withValue(NoteContract.Notes.COL_ACTIVE, note.getActive())
                                .withValue(NoteContract.Notes.COL_SPELL_CHECK, note.getSpellCheck())
                                .withValue(NoteContract.Notes.COL_PIN_ORDER, note.getPinOrder().toString())
                                .withValue(NoteContract.Notes.COL_DATE_CREATED, note.getDateCreated().toString())
                                .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, note.getDateArchived().toString())
                                .withValue(NoteContract.Notes.COL_DATE_MODIFIED, note.getDateModified().toString())
                                .withValue(NoteContract.Notes.COL_DATE_SYNC, note.getDateSync())
                                .withValue(NoteContract.Notes.COL_OWNER, note.getOwner())
                                .build());
                        syncResult.stats.numInserts++;
                    }

                    if (localEntries.size() > 0) {
                        Retrofit remoteRetrofit = RetroInstance.getRetrofitInstance();
                        NotesDataService remoteNotesApi = remoteRetrofit.create(NotesDataService.class);
                        Call<Note[]> callRemote = remoteNotesApi.updateNote(new ArrayList<>(localEntries.values()));
                        callRemote.enqueue(new AppCallback<Note[]>(getContext()) {
                            @Override
                            public void onResponse(Note[] response) {
                                try {
                                    ArrayList<ContentProviderOperation> responseBatch = new ArrayList<>();

                                    for(Note note: response) {
                                        responseBatch.add(ContentProviderOperation.newUpdate(NoteContract.Notes.CONTENT_URI)
                                                .withSelection(NoteContract.Notes.COL_ID + "='" + note.getId() + "'", null)
                                                .withValue(NoteContract.Notes.COL_ID, note.getId())
                                                .withValue(NoteContract.Notes.COL_HEADER, note.getHeader())
                                                .withValue(NoteContract.Notes.COL_TEXT, note.getText())
                                                .withValue(NoteContract.Notes.COL_USER_ID, note.getUserId())
                                                .withValue(NoteContract.Notes.COL_COLOUR, note.getColour())
                                                .withValue(NoteContract.Notes.COL_SELECTION, note.getSelection())
                                                .withValue(NoteContract.Notes.COL_ARCHIVED, note.getArchived())
                                                .withValue(NoteContract.Notes.COL_PINNED, note.getPinned())
                                                .withValue(NoteContract.Notes.COL_ACTIVE, note.getActive())
                                                .withValue(NoteContract.Notes.COL_SPELL_CHECK, note.getSpellCheck())
                                                .withValue(NoteContract.Notes.COL_PIN_ORDER, note.getPinOrder().toString())
                                                .withValue(NoteContract.Notes.COL_DATE_CREATED, note.getDateCreated().toString())
                                                .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, note.getDateArchived().toString())
                                                .withValue(NoteContract.Notes.COL_DATE_MODIFIED, note.getDateModified().toString())
                                                .withValue(NoteContract.Notes.COL_DATE_SYNC, note.getDateSync())
                                                .withValue(NoteContract.Notes.COL_OWNER, note.getOwner())
                                                .build());
                                    }

                                    resolver.applyBatch(NoteContract.CONTENT_AUTHORITY, responseBatch);
                                    resolver.notifyChange(NoteContract.Notes.CONTENT_URI, // URI where data was modified
                                            null, // No local observer
                                            ContentResolver.NOTIFY_UPDATE); // IMPORTANT: Do not sync to network

                                } catch (Exception ex) {
                                   Log.e(TAG, "Error updating local data", ex) ;
                                }
                                Log.i("SERVER", String.valueOf(response.length) + " Items updated and synced successfully");
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                ;
                            }
                        });
                    }


                    Log.i(TAG, "Merge solution ready, applying batch update...");
                    resolver.applyBatch(NoteContract.CONTENT_AUTHORITY, batch);
                    resolver.notifyChange(NoteContract.Notes.CONTENT_URI, // URI where data was modified
                            null, // No local observer
                            ContentResolver.NOTIFY_INSERT | ContentResolver.NOTIFY_UPDATE); // IMPORTANT: Do not sync to network

                    Log.i(TAG, "Finished synchronization!");

                } catch (Exception ex) {
                    Log.e(TAG, "Error synchronizing!", ex);
                    syncResult.stats.numAuthExceptions++;
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    private void archives(Map<String, Note> localEntries, ArrayList<ContentProviderOperation> batch, Note localNote, Note remoteNote) {
        if (remoteNote != null) {
            if (localNote.getDateArchived() != null && remoteNote.getDateArchived() != null) {
                int comp = localNote.getDateArchived().compareTo(remoteNote.getDateArchived());
                if (comp < 0) {
                    batch.add(ContentProviderOperation.newUpdate(NoteContract.Notes.CONTENT_URI)
                            .withSelection(NoteContract.Notes.COL_ID + "='" + remoteNote.getId() + "'", null)
                            .withValue(NoteContract.Notes.COL_ID, remoteNote.getId())
                            .withValue(NoteContract.Notes.COL_HEADER, remoteNote.getHeader())
                            .withValue(NoteContract.Notes.COL_TEXT, remoteNote.getText())
                            .withValue(NoteContract.Notes.COL_USER_ID, remoteNote.getUserId())
                            .withValue(NoteContract.Notes.COL_COLOUR, remoteNote.getColour())
                            .withValue(NoteContract.Notes.COL_SELECTION, remoteNote.getSelection())
                            .withValue(NoteContract.Notes.COL_ARCHIVED, remoteNote.getArchived())
                            .withValue(NoteContract.Notes.COL_PINNED, remoteNote.getPinned())
                            .withValue(NoteContract.Notes.COL_ACTIVE, remoteNote.getActive())
                            .withValue(NoteContract.Notes.COL_SPELL_CHECK, remoteNote.getSpellCheck())
                            .withValue(NoteContract.Notes.COL_PIN_ORDER, remoteNote.getPinOrder().toString())
                            .withValue(NoteContract.Notes.COL_DATE_CREATED, remoteNote.getDateCreated().toString())
                            .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, remoteNote.getDateArchived().toString())
                            .withValue(NoteContract.Notes.COL_DATE_MODIFIED, remoteNote.getDateModified().toString())
                            .withValue(NoteContract.Notes.COL_DATE_SYNC, remoteNote.getDateSync())
                            .withValue(NoteContract.Notes.COL_OWNER, remoteNote.getOwner())
                            .build());
                }

                if (comp > 0) {
                    localEntries.put(localNote.getId() + localNote.getUserId(), localNote);
                }
            }

            if (localNote.getDateArchived() != null && remoteNote.getDateArchived() == null) {
                localEntries.put(localNote.getId() + localNote.getUserId(), localNote);
            }

            if (localNote.getDateArchived() == null && remoteNote.getDateArchived() != null) {
                batch.add(ContentProviderOperation.newUpdate(NoteContract.Notes.CONTENT_URI)
                        .withSelection(NoteContract.Notes.COL_ID + "='" + remoteNote.getId() + "'", null)
                        .withValue(NoteContract.Notes.COL_ID, remoteNote.getId())
                        .withValue(NoteContract.Notes.COL_HEADER, remoteNote.getHeader())
                        .withValue(NoteContract.Notes.COL_TEXT, remoteNote.getText())
                        .withValue(NoteContract.Notes.COL_USER_ID, remoteNote.getUserId())
                        .withValue(NoteContract.Notes.COL_COLOUR, remoteNote.getColour())
                        .withValue(NoteContract.Notes.COL_SELECTION, remoteNote.getSelection())
                        .withValue(NoteContract.Notes.COL_ARCHIVED, remoteNote.getArchived())
                        .withValue(NoteContract.Notes.COL_PINNED, remoteNote.getPinned())
                        .withValue(NoteContract.Notes.COL_ACTIVE, remoteNote.getActive())
                        .withValue(NoteContract.Notes.COL_SPELL_CHECK, remoteNote.getSpellCheck())
                        .withValue(NoteContract.Notes.COL_PIN_ORDER, remoteNote.getPinOrder().toString())
                        .withValue(NoteContract.Notes.COL_DATE_CREATED, remoteNote.getDateCreated().toString())
                        .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, remoteNote.getDateArchived().toString())
                        .withValue(NoteContract.Notes.COL_DATE_MODIFIED, remoteNote.getDateModified().toString())
                        .withValue(NoteContract.Notes.COL_DATE_SYNC, remoteNote.getDateSync())
                        .withValue(NoteContract.Notes.COL_OWNER, remoteNote.getOwner())
                        .build());
            }
        }
    }

    private void pinOrder(Map<String, Note> localEntries, ArrayList<ContentProviderOperation> batch, Note localNote, Note remoteNote) {
        if (remoteNote != null) {
            if (localNote.getPinOrder() != null && remoteNote.getPinOrder() != null) {
                int comp = localNote.getPinOrder().compareTo(remoteNote.getPinOrder());
                if (comp < 0) {
                    batch.add(ContentProviderOperation.newUpdate(NoteContract.Notes.CONTENT_URI)
                            .withSelection(NoteContract.Notes.COL_ID + "='" + remoteNote.getId() + "'", null)
                            .withValue(NoteContract.Notes.COL_ID, remoteNote.getId())
                            .withValue(NoteContract.Notes.COL_HEADER, remoteNote.getHeader())
                            .withValue(NoteContract.Notes.COL_TEXT, remoteNote.getText())
                            .withValue(NoteContract.Notes.COL_USER_ID, remoteNote.getUserId())
                            .withValue(NoteContract.Notes.COL_COLOUR, remoteNote.getColour())
                            .withValue(NoteContract.Notes.COL_SELECTION, remoteNote.getSelection())
                            .withValue(NoteContract.Notes.COL_ARCHIVED, remoteNote.getArchived())
                            .withValue(NoteContract.Notes.COL_PINNED, remoteNote.getPinned())
                            .withValue(NoteContract.Notes.COL_ACTIVE, remoteNote.getActive())
                            .withValue(NoteContract.Notes.COL_SPELL_CHECK, remoteNote.getSpellCheck())
                            .withValue(NoteContract.Notes.COL_PIN_ORDER, remoteNote.getPinOrder().toString())
                            .withValue(NoteContract.Notes.COL_DATE_CREATED, remoteNote.getDateCreated().toString())
                            .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, remoteNote.getDateArchived().toString())
                            .withValue(NoteContract.Notes.COL_DATE_MODIFIED, remoteNote.getDateModified().toString())
                            .withValue(NoteContract.Notes.COL_DATE_SYNC, remoteNote.getDateSync())
                            .withValue(NoteContract.Notes.COL_OWNER, remoteNote.getOwner())
                            .build());
                }

                if (comp > 0) {
                    localEntries.put(localNote.getId() + localNote.getUserId(), localNote);
                }
            }

            if (localNote.getPinOrder() != null && remoteNote.getPinOrder() == null) {
                localEntries.put(localNote.getId() + localNote.getUserId(), localNote);
            }

            if (localNote.getPinOrder() == null && remoteNote.getPinOrder() != null) {
                batch.add(ContentProviderOperation.newUpdate(NoteContract.Notes.CONTENT_URI)
                        .withSelection(NoteContract.Notes.COL_ID + "='" + remoteNote.getId() + "'", null)
                        .withValue(NoteContract.Notes.COL_ID, remoteNote.getId())
                        .withValue(NoteContract.Notes.COL_HEADER, remoteNote.getHeader())
                        .withValue(NoteContract.Notes.COL_TEXT, remoteNote.getText())
                        .withValue(NoteContract.Notes.COL_USER_ID, remoteNote.getUserId())
                        .withValue(NoteContract.Notes.COL_COLOUR, remoteNote.getColour())
                        .withValue(NoteContract.Notes.COL_SELECTION, remoteNote.getSelection())
                        .withValue(NoteContract.Notes.COL_ARCHIVED, remoteNote.getArchived())
                        .withValue(NoteContract.Notes.COL_PINNED, remoteNote.getPinned())
                        .withValue(NoteContract.Notes.COL_ACTIVE, remoteNote.getActive())
                        .withValue(NoteContract.Notes.COL_SPELL_CHECK, remoteNote.getSpellCheck())
                        .withValue(NoteContract.Notes.COL_PIN_ORDER, remoteNote.getPinOrder().toString())
                        .withValue(NoteContract.Notes.COL_DATE_CREATED, remoteNote.getDateCreated().toString())
                        .withValue(NoteContract.Notes.COL_DATE_ARCHIVED, remoteNote.getDateArchived().toString())
                        .withValue(NoteContract.Notes.COL_DATE_MODIFIED, remoteNote.getDateModified().toString())
                        .withValue(NoteContract.Notes.COL_DATE_SYNC, remoteNote.getDateSync())
                        .withValue(NoteContract.Notes.COL_OWNER, remoteNote.getOwner())
                        .build());
            }
        }
    }

    /**
     * A blocking method to stream the server's content and build it into a string.
     *
     * @param url API call
     * @return String response
     */
    private String download(String url) throws IOException {
        // Ensure we ALWAYS close these!
        HttpURLConnection client = null;
        InputStream is = null;

        try {
            // Connect to the server using GET protocol
            URL server = new URL(url);
            client = (HttpURLConnection) server.openConnection();
            client.connect();

            // Check for valid response code from the server
            int status = client.getResponseCode();
            is = (status == HttpURLConnection.HTTP_OK)
                    ? client.getInputStream() : client.getErrorStream();

            // Build the response or error as a string
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String temp; ((temp = br.readLine()) != null); ) {
                sb.append(temp);
            }

            return sb.toString();
        } finally {
            if (is != null) {
                is.close();
            }
            if (client != null) {
                client.disconnect();
            }
        }
    }
}
