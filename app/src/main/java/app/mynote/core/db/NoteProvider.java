package app.mynote.core.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NoteProvider extends ContentProvider {
    // Use ints to represent different queries
    private static final int NOTE = 1;
    private static final int NOTE_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        // Add all our query types to our UriMatcher
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTE);
        uriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES + "/#", NOTE_ID);
    }

    private SQLiteDatabase db;


    @Override
    public boolean onCreate() {
        this.db = DatabaseClient.getInstance(getContext()).getDb();
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Find the MIME type of the results... multiple results or a single result
        switch (uriMatcher.match(uri)) {
            case NOTE:
                return NoteContract.Notes.CONTENT_TYPE;
            case NOTE_ID:
                return NoteContract.Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Invalid URI!");
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor c;
        switch (uriMatcher.match(uri)) {
            // Query for multiple article results
            case NOTE:
                c = db.query(NoteContract.Notes.NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Query for single article result
            case NOTE_ID:
                long _id = ContentUris.parseId(uri);
                c = db.query(NoteContract.Notes.NAME,
                        projection,
                        NoteContract.Notes.COL_ID + "=?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI!");
        }

        // Tell the cursor to register a content observer to observe changes to the
        // URI or its descendants.
        assert getContext() != null;
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri returnUri;
        long _id;

        switch (uriMatcher.match(uri)) {
            case NOTE:
                _id = db.insert(NoteContract.Notes.NAME, null, values);
                returnUri = ContentUris.withAppendedId(NoteContract.Notes.CONTENT_URI, _id);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI!");
        }

        // Notify any observers to update the UI
        assert getContext() != null;
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows;
        switch (uriMatcher.match(uri)) {
            case NOTE:
                rows = db.update(NoteContract.Notes.NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI!");
        }

        // Notify any observers to update the UI
        if (rows != 0) {
            assert getContext() != null;
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows;
        switch (uriMatcher.match(uri)) {
            case NOTE:
                rows = db.delete(NoteContract.Notes.NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI!");
        }

        // Notify any observers to update the UI
        if (rows != 0) {
            assert getContext() != null;
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

}
