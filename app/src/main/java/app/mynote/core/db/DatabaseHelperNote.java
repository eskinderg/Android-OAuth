package app.mynote.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import app.mynote.core.utils.AppTimestamp;
import app.mynote.fragments.note.Note;

public class DatabaseHelperNote extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NOTES = "notes";
    private static final String KEY_ID = "id";
    private static final String KEY_HEADER = "header";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_COLOUR = "colour";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_LEFT = "left";
    private static final String KEY_TOP = "top";
    private static final String KEY_SELECTION = "selection";
    private static final String KEY_ARCHIVED = "archived";
    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_SPELL_CHECK = "spellCheck";
    private static final String KEY_PIN_ORDER = "pinOrder";
    private static final String KEY_DATE_CREATED = "dateCreated";
    private static final String KEY_DATE_MODIFIED = "dateModified";
    private static final String KEY_DATE_ARCHIVED = "dateArchived";
    private static final String KEY_DATE_SYNC = "dateSync";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_TEXT = "text";
    private static final String CREATE_TABLE_NOTES = "CREATE TABLE "
            + TABLE_NOTES + "(" + KEY_ID
            + " TEXT PRIMARY KEY NOT NULL ," +
            KEY_HEADER + " TEXT, " +
            KEY_USER_ID + " TEXT, " +
            KEY_COLOUR + " TEXT, " +
            KEY_HEIGHT + " INTEGER, " +
            KEY_WIDTH + " INTEGER, " +
            KEY_LEFT + " INTEGER, " +
            KEY_TOP + " INTEGER, " +
            KEY_SELECTION + " TEXT, " +
            KEY_ARCHIVED + " TINYINT, " +
            KEY_FAVORITE + " TINYINT, " +
            KEY_ACTIVE + " TINYINT, " +
            KEY_SPELL_CHECK + " TINYINT, " +
            KEY_PIN_ORDER + " TEXT, " +
            KEY_DATE_CREATED + " TEXT, " +
            KEY_DATE_MODIFIED + " TEXT, " +
            KEY_DATE_ARCHIVED + " TEXT, " +
            KEY_DATE_SYNC + " TEXT, " +
            KEY_OWNER + " TEXT, " +
            KEY_TEXT + " TEXT " +
            "); ";
    public static String DATABASE_NAME = "note_db.sqlite";

    public DatabaseHelperNote(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.d("table", CREATE_TABLE_NOTES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_NOTES + "'");
        onCreate(db);
    }

    public long addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, note.getId());
        values.put(KEY_HEADER, note.getHeader());
        values.put(KEY_SELECTION, note.getSelection());
        values.put(KEY_ARCHIVED, note.getArchived());
        values.put(KEY_COLOUR, note.getColour());
        values.put(KEY_ACTIVE, note.isActive());
        values.put(KEY_SPELL_CHECK, note.getSpellCheck());
        values.put(KEY_PIN_ORDER, note.getPinOrder().toString());
        values.put(KEY_DATE_CREATED, note.getDateCreated());
        values.put(KEY_DATE_MODIFIED, note.getDateModified().toString());
        values.put(KEY_DATE_ARCHIVED, note.getDateArchived().toString());
        values.put(KEY_DATE_SYNC, note.getDateSync());
        values.put(KEY_OWNER, note.getOwner());
        values.put(KEY_TEXT, note.getText());
        long insert = db.insert(TABLE_NOTES, null, values);

        return insert;
    }

    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> noteArrayList = new ArrayList<Note>();

        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(c.getString(c.getColumnIndex(KEY_ID)));
                note.setHeader(c.getString(c.getColumnIndex(KEY_HEADER)));
                note.setSelection(c.getString(c.getColumnIndex(KEY_SELECTION)));
                note.setColour(c.getString(c.getColumnIndex(KEY_COLOUR)));
                note.setArchived(c.getInt(c.getColumnIndex(KEY_ARCHIVED)) > 0);
                note.setActive(c.getInt(c.getColumnIndex(KEY_ACTIVE)) > 0);
                note.setSpellCheck(c.getInt(c.getColumnIndex(KEY_SPELL_CHECK)) > 0);
                note.setPinOrder(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(KEY_PIN_ORDER))));
                note.setDateCreated(c.getString(c.getColumnIndex(KEY_DATE_CREATED)));
                note.setDateModified(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(KEY_DATE_MODIFIED))));
                note.setDateArchived(AppTimestamp.convertStringToTimestamp(c.getString(c.getColumnIndex(KEY_DATE_ARCHIVED))));
                note.setDateSync(c.getString(c.getColumnIndex(KEY_DATE_SYNC)));
                note.setOwner(c.getString(c.getColumnIndex(KEY_OWNER)));
                note.setText(c.getString(c.getColumnIndex(KEY_TEXT)));
                // adding to list
                noteArrayList.add(note);
            } while (c.moveToNext());
        }
        return noteArrayList;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_HEADER, note.getHeader());
        values.put(KEY_SELECTION, note.getSelection());
        values.put(KEY_ARCHIVED, note.getArchived());
        values.put(KEY_COLOUR, note.getColour());
        values.put(KEY_ACTIVE, note.isActive());
        values.put(KEY_SPELL_CHECK, note.getSpellCheck());
        values.put(KEY_PIN_ORDER, note.getPinOrder().toString());
        values.put(KEY_DATE_CREATED, note.getDateCreated());
        values.put(KEY_DATE_MODIFIED, note.getDateModified().toString());
        values.put(KEY_DATE_ARCHIVED, note.getDateArchived().toString());
        values.put(KEY_DATE_SYNC, note.getDateSync());
        values.put(KEY_OWNER, note.getOwner());
        values.put(KEY_TEXT, note.getText());

        return db.update(TABLE_NOTES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

}
