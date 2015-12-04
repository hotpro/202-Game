/*
 * Project: asqare
 * Copyright (C) 2008-2012 rdrr.labs@gmail.com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.sjsu.cmpe202.gamelist;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.util.Log;

//-----------------------------------------------

/*
 * Debug Tip; to view content of database, use:
 * $ cd /cygdrive/c/.../android-sdk_..._windows/tools
 * $ ./adb shell 'echo ".dump" | sqlite3 data/data/com.alfray.asqare/databases/games.db'
 */

/**
 * Provides access to games stored by asqare
 */
public class GameListProvider extends ContentProvider {

	/** 2nd segment of mime-type used by the game list provider */
    public static final String MIME_TYPE = "vnd.alfray.asqare.game";

    /** The authority part of the content://com.alfray.asqare/... URI */
    public static final String URI_AUTHORITY = "edu.sjsu.cmpe202.gamelist";

    /** The path part of the content://.../games URI */
    public static final String URI_PATH = "games";

    /** The content:// style URL for this table */
    public static final Uri CONTENT_URI = Uri.parse("content://" + URI_AUTHORITY + "/" + URI_PATH);

    /** The path part of the content://.../new/ games URI */
    public static final String NEW_PATH = "new";

    /** The content:// style URL for creating new games. An FQCN gameplay must be appended. */
    public static final Uri NEW_URI = Uri.parse("content://" + URI_AUTHORITY + "/" + NEW_PATH);


    private static final String TAG = "Asqare.GameListProvider";
    private static final String TABLE_NAME = "games";
    private static final String DB_NAME = "games.db";
    private static final int DB_VERSION = 1 * 100 + 1; // major*100 + minor

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final HashMap<String, String> sProjectionMap = new HashMap<String, String>();
    private static final int MATCH_ALL_GAMES = 1;
    private static final int MATCH_GAME_ID = 2;
    private static final int MATCH_NEW_GAME_CLASS = 3;

    static {
        sUriMatcher.addURI(URI_AUTHORITY, URI_PATH,        MATCH_ALL_GAMES);
        sUriMatcher.addURI(URI_AUTHORITY, URI_PATH + "/#", MATCH_GAME_ID);
        sUriMatcher.addURI(URI_AUTHORITY, NEW_PATH + "/*", MATCH_NEW_GAME_CLASS);

        sProjectionMap.put(Columns._ID,         Columns._ID);
        sProjectionMap.put(Columns.GAMEPLAY,    Columns.GAMEPLAY);
        sProjectionMap.put(Columns.SCORE,       Columns.SCORE);
        sProjectionMap.put(Columns.STATE,       Columns.STATE);
        sProjectionMap.put(Columns.CREATED_MS,  Columns.CREATED_MS);
        sProjectionMap.put(Columns.MODIFIED_MS, Columns.MODIFIED_MS);
    }

    private SQLiteDatabase mDb;

    // ----------------------------------

    @Override
    public boolean onCreate() {
        DatabaseHelper helper = new DatabaseHelper(getContext(), DB_NAME, null /* cursor factory */, DB_VERSION);
        mDb = helper.getWritableDatabase();
        boolean created = mDb != null;
        return created;
    }

    // ----------------------------------

    @Override
    public String getType(Uri uri) {
    	String result;
        switch(sUriMatcher.match(uri)) {
        case MATCH_GAME_ID:
            result = "vnd.android.cursor.item/" + MIME_TYPE;
            break;
        case MATCH_NEW_GAME_CLASS:
        	// breakthrough, the GameplayActivity expects a cursor.dir mimetype for inserts
        case MATCH_ALL_GAMES:
            result = "vnd.android.cursor.dir/" + MIME_TYPE;
            break;
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        switch(sUriMatcher.match(uri)) {
        case MATCH_GAME_ID:
            whereClause = addWhereId(uri, whereClause);
            int count = mDb.delete(TABLE_NAME, whereClause, whereArgs);
            getContext().getContentResolver().notifyChange(uri, null /* observer */);
            return count;
        case MATCH_ALL_GAMES:
        case MATCH_NEW_GAME_CLASS:
            // breakthrough, we don't delete the container or new games
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    /**
     * Helper that returns a where clause "_id=NN" where NN is the last segment of
     * the input URI. If there's an existing whereClause, it is rewritten using
     * "_id=NN AND ( whereClause )".
     */
	private String addWhereId(Uri uri, String whereClause) {
		if (whereClause != null && whereClause.length() > 0) {
		    whereClause = "AND (" + whereClause + ")";
		} else {
		    whereClause = "";
		}
		whereClause = String.format("%1$s=%2$d %3$s",
		        Columns._ID,
		        // Long.parseLong(uri.getPathSegments().get(1)), // will throw if not a long
		        ContentUris.parseId(uri),
		        whereClause);
		return whereClause;
	}

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch(sUriMatcher.match(uri)) {
        case MATCH_ALL_GAMES:
        case MATCH_NEW_GAME_CLASS:
            // processed below
            break;
        case MATCH_GAME_ID:
            // breakthrough, we can't insert a note with a specific ID, this method just computes
            // its own id and returns the new URI.
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        // the last segment must be the FQCN to create

        // fail for missing values: gameplay must be specified
        if (values == null) {
            throw new IllegalArgumentException("Missing insert ContentValues");
        } else if (!values.containsKey(Columns.GAMEPLAY)) {
            throw new IllegalArgumentException("Missing 'gameplay' in insert ContentValues");
        }

        // set default values: state & score can be empty, created/modified_ms can be implied
        if (!values.containsKey(Columns.STATE)) {
            values.put(Columns.STATE, "");
        }
        if (!values.containsKey(Columns.SCORE)) {
            values.put(Columns.SCORE, "N/A");
        }
        Long now_ms = System.currentTimeMillis();  // values below will need a boxed long
        if (!values.containsKey(Columns.CREATED_MS)) {
            values.put(Columns.CREATED_MS, now_ms);
        }
        if (!values.containsKey(Columns.MODIFIED_MS)) {
            values.put(Columns.MODIFIED_MS, now_ms);
        }

        long id = mDb.insert(TABLE_NAME, Columns.GAMEPLAY, values);
        if (id < 0) throw new SQLException("insert failed for " + uri);

        uri = ContentUris.withAppendedId(CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {

    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	qb.setTables(TABLE_NAME);

        switch(sUriMatcher.match(uri)) {
        case MATCH_GAME_ID:
        	qb.appendWhere(String.format("%1$s=%2$d", Columns._ID, ContentUris.parseId(uri)));
            break;
        case MATCH_ALL_GAMES:
        	qb.setProjectionMap(sProjectionMap);
            break;
        case MATCH_NEW_GAME_CLASS:
        	// breakthrough, you can't query new game creations
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        if (sortOrder == null || sortOrder.length() == 0) sortOrder = Columns.DEFAULT_SORT_ORDER;

        Cursor c = qb.query(mDb, projection, selection, selectionArgs,
        		null, // groupBy
        		null, // having,
        		sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        switch(sUriMatcher.match(uri)) {
        case MATCH_GAME_ID:
        	whereClause = addWhereId(uri, whereClause);
        	// continue below
            break;
        case MATCH_ALL_GAMES:
        	// processed below
            break;
        case MATCH_NEW_GAME_CLASS:
        	// breakthrough, you can't update new game creations
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    	int count = mDb.update(TABLE_NAME, values, whereClause, whereArgs);
    	getContext().getContentResolver().notifyChange(uri, null /* observer */);
        return count;
    }


    // ----------------------------------

    /** Convenience helper to open/create/update the database */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(String.format("CREATE TABLE %1$s "
                    + "(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "%3$s TEXT, "
                    + "%4$s TEXT, "
                    + "%5$s TEXT, "
                    + "%6$s INTEGER, "
                    + "%7$s INTEGER);" ,
                    TABLE_NAME,
                    Columns._ID,
                    Columns.GAMEPLAY,
                    Columns.SCORE,
                    Columns.STATE,
                    Columns.CREATED_MS,
                    Columns.MODIFIED_MS));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, String.format("Upgrading database from version %1$d to %2$d.",
                    oldVersion, newVersion));
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            // pass
        }
    }

}
