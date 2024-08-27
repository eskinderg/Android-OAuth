package app.mynote.fragments.note;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import app.mynote.auth.AuthConfig;
import app.mynote.core.db.NoteContract;

public class NoteService {

    private Context context;
    private ContentResolver contentResolver;

    public NoteService(Context c) {
        this.context = c;
        contentResolver = c.getContentResolver();
    }

    public Note add(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteContract.Notes.COL_ID, note.getId());
        values.put(NoteContract.Notes.COL_TEXT, note.getText());
        values.put(NoteContract.Notes.COL_HEADER, note.getHeader());
        values.put(NoteContract.Notes.COL_USER_ID, AuthConfig.USER.getId());
        values.put(NoteContract.Notes.COL_SELECTION, note.getSelection());
        values.put(NoteContract.Notes.COL_ARCHIVED, note.getArchived());
        values.put(NoteContract.Notes.COL_PINNED, note.getPinned());
        values.put(NoteContract.Notes.COL_COLOUR, note.getColour());
        values.put(NoteContract.Notes.COL_ACTIVE, note.isActive());
        values.put(NoteContract.Notes.COL_SPELL_CHECK, note.getSpellCheck());
        values.put(NoteContract.Notes.COL_PIN_ORDER, note.getPinOrder());
        values.put(NoteContract.Notes.COL_DATE_CREATED, note.getDateCreated());
        values.put(NoteContract.Notes.COL_DATE_MODIFIED, note.getDateModified());
        values.put(NoteContract.Notes.COL_DATE_ARCHIVED, note.getDateArchived());
        values.put(NoteContract.Notes.COL_DATE_SYNC, note.getDateSync());
        values.put(NoteContract.Notes.COL_OWNER, note.getOwner());
        contentResolver.insert(NoteContract.Notes.CONTENT_URI, values);
        return note;
    }

    public Note update(Note note, boolean markModified) {
        ContentValues values = new ContentValues();
        values.put(NoteContract.Notes.COL_TEXT, note.getText());
        values.put(NoteContract.Notes.COL_HEADER, note.getHeader());
        values.put(NoteContract.Notes.COL_USER_ID, AuthConfig.USER.getId());
        values.put(NoteContract.Notes.COL_SELECTION, note.getSelection());
        values.put(NoteContract.Notes.COL_ARCHIVED, note.getArchived());
        values.put(NoteContract.Notes.COL_PINNED, note.getPinned());
        values.put(NoteContract.Notes.COL_COLOUR, note.getColour());
        values.put(NoteContract.Notes.COL_ACTIVE, note.isActive());
        values.put(NoteContract.Notes.COL_SPELL_CHECK, note.getSpellCheck());
        values.put(NoteContract.Notes.COL_PIN_ORDER, note.getPinOrder());
        values.put(NoteContract.Notes.COL_DATE_CREATED, note.getDateCreated());
        if(markModified){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = new Date();
            values.put(NoteContract.Notes.COL_DATE_MODIFIED, dateFormat.format(date));
        }else{
            values.put(NoteContract.Notes.COL_DATE_MODIFIED, note.getDateModified());
        }
        values.put(NoteContract.Notes.COL_DATE_ARCHIVED, note.getDateArchived());
        values.put(NoteContract.Notes.COL_DATE_SYNC, note.getDateSync());
        values.put(NoteContract.Notes.COL_OWNER, note.getOwner());
        contentResolver.update(NoteContract.Notes.CONTENT_URI, values, NoteContract.Notes.COL_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        return note;
    }


    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> noteArrayList = new ArrayList<Note>();
//        String[] where ={ NoteContract.Notes.COL_USER_ID + " = " + AuthConfig.USER.getId();

        Cursor c = contentResolver.query(NoteContract.Notes.CONTENT_URI, null, NoteContract.Notes.COL_USER_ID + " = '" + AuthConfig.USER.getId() + "'", null, null, null);
        assert c != null;
        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(c.getString(c.getColumnIndex(NoteContract.Notes.COL_ID)));
                note.setHeader(c.getString(c.getColumnIndex(NoteContract.Notes.COL_HEADER)));
                note.setUserId(c.getString(c.getColumnIndex(NoteContract.Notes.COL_USER_ID)));
                note.setText(c.getString(c.getColumnIndex(NoteContract.Notes.COL_TEXT)));
                note.setSelection(c.getString(c.getColumnIndex(NoteContract.Notes.COL_SELECTION)));
                note.setColour(c.getString(c.getColumnIndex(NoteContract.Notes.COL_COLOUR)));
                note.setArchived(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_ARCHIVED)) > 0);
                note.setPinned(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_PINNED)) > 0);
                note.setActive(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_ACTIVE)) > 0);
                note.setSpellCheck(c.getInt(c.getColumnIndex(NoteContract.Notes.COL_SPELL_CHECK)) > 0);
                note.setPinOrder(c.getString(c.getColumnIndex(NoteContract.Notes.COL_PIN_ORDER)));
                note.setDateCreated(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_CREATED)));
                note.setDateModified(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_MODIFIED)));
                note.setDateArchived(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_ARCHIVED)));
                note.setDateSync(c.getString(c.getColumnIndex(NoteContract.Notes.COL_DATE_SYNC)));
                note.setOwner(c.getString(c.getColumnIndex(NoteContract.Notes.COL_OWNER)));
                // adding to list
                noteArrayList.add(note);
            } while (c.moveToNext());
        }
        c.close();
        return noteArrayList;
    }

    public ArrayList<Note> getArchived() {
        ArrayList<Note> response = getAllNotes();
        return new ArrayList<>(response.stream().filter(n -> n.getArchived()).collect(Collectors.toList()));
    }

    public ArrayList<Note> getPinned() {
        ArrayList<Note> response = getAllNotes();
        return new ArrayList<>(response.stream().filter(n -> n.isPinned()).collect(Collectors.toList()));
    }

    public ArrayList<Note> Update(ArrayList<Note> notes) {
        for (Note note : notes) {
            ContentValues values = new ContentValues();
            values.put(NoteContract.Notes.COL_ID, note.getId());
            values.put(NoteContract.Notes.COL_TEXT, note.getText());
            values.put(NoteContract.Notes.COL_HEADER, note.getHeader());
            values.put(NoteContract.Notes.COL_USER_ID, AuthConfig.USER.getId());
            values.put(NoteContract.Notes.COL_SELECTION, note.getSelection());
            values.put(NoteContract.Notes.COL_ARCHIVED, note.getArchived());
            values.put(NoteContract.Notes.COL_PINNED, note.getPinned());
            values.put(NoteContract.Notes.COL_COLOUR, note.getColour());
            values.put(NoteContract.Notes.COL_ACTIVE, note.isActive());
            values.put(NoteContract.Notes.COL_SPELL_CHECK, note.getSpellCheck());
            values.put(NoteContract.Notes.COL_PIN_ORDER, note.getPinOrder());
            values.put(NoteContract.Notes.COL_DATE_CREATED, note.getDateCreated());
            values.put(NoteContract.Notes.COL_DATE_MODIFIED, note.getDateModified());
            values.put(NoteContract.Notes.COL_DATE_ARCHIVED, note.getDateArchived());
            values.put(NoteContract.Notes.COL_DATE_SYNC, note.getDateSync());
            values.put(NoteContract.Notes.COL_OWNER, note.getOwner());
            update(note, true);
        }
        return notes;
    }
}
