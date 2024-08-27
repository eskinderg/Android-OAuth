package app.mynote.core.db;

import android.net.Uri;

public final class NoteContract {
    public static final String CONTENT_AUTHORITY = "app.mynote.sync";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_NOTES = "notes";

    // Database info
    static final String DB_NAME = "notes_db";
    static final int DB_VERSION = 1;


    public static abstract class Notes {
        public static final String NAME = "notes";
        public static final String COL_ID = "id";
        public static final String COL_HEADER = "header";
        public static final String COL_TEXT = "text";
        public static final String COL_USER_ID = "userId";
        public static final String COL_COLOUR = "colour";
        public static final String COL_SELECTION = "selection";
        public static final String COL_ARCHIVED = "archived";
        public static final String COL_PINNED = "pinned";
        public static final String COL_ACTIVE = "active";
        public static final String COL_SPELL_CHECK = "spellCheck";
        public static final String COL_PIN_ORDER = "pinOrder";
        public static final String COL_DATE_CREATED = "dateCreated";
        public static final String COL_DATE_MODIFIED = "dateModified";
        public static final String COL_DATE_ARCHIVED = "dateArchived";
        public static final String COL_DATE_SYNC = "dateSync";
        public static final String COL_OWNER = "owner";


        // ContentProvider information for notes
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTES).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_NOTES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_NOTES;
    }
}
