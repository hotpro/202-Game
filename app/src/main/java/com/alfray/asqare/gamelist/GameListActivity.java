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

package com.alfray.asqare.gamelist;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.alfray.asqare.AsqareActivity;
import com.alfray.asqare.AsqareContext;
import com.alfray.asqare.R;
import com.alfray.asqare.gameplay.AvailableGameplays;
import com.alfray.asqare.gameplay.Bijoux;
import com.alfray.asqare.gameplay.Gameplay;
import com.alfray.asqare.gameplay.Title;

//-----------------------------------------------

public class GameListActivity extends AsqareActivity {

	private static final int GROUP_HAS_SELECTION = 42;

	private static final String TAG = "GameListActivity";

    /** The columns we are interested in from the database */
    private static final String[] PROJECTION = new String[] {
    	Columns._ID,
    	Columns.GAMEPLAY,
    	Columns.CREATED_MS,
    	Columns.MODIFIED_MS,
    	Columns.SCORE,
    	Columns.STATE
    };

	private ListView mListView;
	private ArrayList<AlertDialog.Builder> mTempDialogList = new ArrayList<AlertDialog.Builder>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle inState) {
    	Log.v(TAG, "onCreate " + (inState == null ? "no" : " with") + " state");
	    super.onCreate(inState);

	    Cursor c = managedQuery(
	    		GameListProvider.CONTENT_URI, // uri
	    		PROJECTION, // projection
	    		null, // selection
	    		null, // selectionArgs
	    		null); // sortOrder

	    // Sanity check (for debugging purposes): all columns adapted below must have
	    // been requested in the projection above. If this is not the case, the adapter
	    // will throw in getColumnIndexOrThrow at creation.
		String[] cols  = { Columns.GAMEPLAY, Columns.MODIFIED_MS, Columns.SCORE, Columns.STATE };
		int[]    views = { R.id.gameplay,    R.id.modified,       R.id.score,    R.id.image    };

		next_col: for (String col : cols) {
			for (String proj : PROJECTION) {
				if (col.equals(proj)) continue next_col;
			}
			throw new AssertionError("DESIGN ERROR: column name not in PROJECTION: " + col);
		}

	    SimpleCursorAdapter a = new SimpleCursorAdapter(
	    		this, // context
	    		R.layout.gameitem, // layout
	    		c, // cursor
	    		cols, // from (column names)
	    		views // to (view ids)
	    		);

	    a.setViewBinder(new MyViewBinder());

	    mListView = (ListView) findViewById(R.id.gamelist);
	    mListView.setAdapter(a);

	    final GameListActivity self = this;
		Button button_new = (Button) findViewById(R.id.new_game);
		button_new.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View view) {
				showNewGame();
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
				self.selectExistingGame(id);
			}
		});

		mListView.requestFocus();

		// create a title gameplay for the background, but only if the layout has a
		// board view
		if (getContext().getBoardView() != null) {
			getContext().createGameplay(Title.class, null /* state */);
			getContext().startGameplay();
		}

		setTitle(getResources().getString(R.string.list_title));
	}

	@Override
	protected void setupWindowAndContent() {
		super.setupWindowAndContent();
		setContentView(R.layout.gamelist);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(GROUP_HAS_SELECTION, R.string.del_game, 0, R.string.del_game).setIcon(R.drawable.delete);
    	menu.add(0, R.string.new_game, 0, R.string.new_game).setIcon(R.drawable.new_game);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(GROUP_HAS_SELECTION,
						     mListView.getSelectedItemPosition() != ListView.INVALID_POSITION);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.string.del_game:
			confirmDeleteGame(mListView.getSelectedItemId(), (Cursor) mListView.getSelectedItem());
			break;
		case R.string.new_game:
			showNewGame();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// --------------------

	protected void showNewGame() {

		AvailableGameplays ag = AvailableGameplays.getInstance();
		final Class<?>[] gs = ag.getAvailableGameplays();
		if (gs.length == 1) {
			newGame(gs[0]);
			return;
		}

		Builder d = new AlertDialog.Builder(this);
		mTempDialogList.add(d);
		final int index = mTempDialogList.indexOf(d);

		d.setCancelable(true);
		d.setTitle(R.string.new_title);
		d.setIcon(R.drawable.new_game);

		String[] items = new String[gs.length];
		for (int i = 0; i < gs.length; i++) {
			items[i] = ag.getDisplayName(getContext(), gs[i]);
		}

		d.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
        		removeDialog(index);
    			newGame(gs[whichButton]);
            }
		});

		showDialog(index);
	}

	private void confirmDeleteGame(final long id, Cursor cursor) {
		if (id == ListView.INVALID_ROW_ID) return;

		long mod_ms = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.MODIFIED_MS));
		long creat_ms = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.CREATED_MS));
		String clazz = cursor.getString(cursor.getColumnIndexOrThrow(Columns.GAMEPLAY));
		String score = cursor.getString(cursor.getColumnIndexOrThrow(Columns.SCORE));

		clazz = formatGameplay(clazz);
		long now = System.currentTimeMillis();
		CharSequence mod = formatTime(mod_ms, now);
		CharSequence creat = formatTime(creat_ms, now);

		Builder d = new AlertDialog.Builder(this);
		mTempDialogList.add(d);
		final int index = mTempDialogList.indexOf(d);

		d.setCancelable(true);
		d.setTitle(R.string.delete_title);
		d.setIcon(R.drawable.delete);

		d.setMessage(getResources().getString(R.string.delete_msg, clazz, creat, mod, score));

		d.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
            public void onCancel(DialogInterface arg0) {
        		removeDialog(index);
			}
		});

		d.setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
        		removeDialog(index);
        		deleteExistingGame(id);
            }
		});

		d.setNegativeButton(R.string.delete_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
        		removeDialog(index);
            }
		});

		showDialog(index);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return mTempDialogList.get(id).create();
	}

	private String formatGameplay(String clazz) {
		int pos = clazz.lastIndexOf('.');
		if (pos > 0) clazz = clazz.substring(pos + 1);
		return clazz;
	}

	private CharSequence formatTime(long time_ms, long now) {
	    /* The following method is not visible anymore in SDK 1.0 but still present,
	       just use reflection to do the equivalent of:

        CharSequence mod = android.pim.DateUtils.getRelativeTimeSpanString(
                time_ms,
                now,
                0); // minresolution
        */
	    ClassLoader cl = getClassLoader();
	    Class<?> du_class;
        try {
            du_class = cl.loadClass("android.pim.DateUtils");
            Method method = du_class.getDeclaredMethod("getRelativeTimeSpanString",
                    new Class[] { long.class, long.class, long.class });
            Object v = method.invoke(null /*receiver*/, new Object[] { time_ms, now, (long)0 });

            if (v instanceof CharSequence) {
                return (CharSequence) v;
            }
        } catch (ClassNotFoundException e) {
            // pass
        } catch (SecurityException e) {
            // pass
        } catch (NoSuchMethodException e) {
            // pass
        } catch (IllegalArgumentException e) {
            // pass
        } catch (IllegalAccessException e) {
            // pass
        } catch (InvocationTargetException e) {
            // pass
        }

	    return null;
	}

	// --------------------

	public class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

		private HashMap<String, Bitmap> mBitmapCache = new HashMap<String, Bitmap>();
		private AsqareContext mLocalAsqareContext;

		public MyViewBinder() {
		}

		@Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view == null || cursor == null || columnIndex < 0) return false;

			// debug
			// Log.v(TAG, String.format("MyViewBinder.setViewValue, col=%s, view=%s",
			//		cursor.getColumnName(columnIndex), view));

    		if (view instanceof TextView && view.getId() == R.id.modified) {
    			long mod_ms = cursor.getLong(columnIndex); // Columns.MODIFIED_MS
				long creat_ms = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.CREATED_MS));
				long now = System.currentTimeMillis();

				CharSequence mod = formatTime(mod_ms, now);
    			CharSequence creat = formatTime(creat_ms, now);

    			String text = getResources().getString(R.string.created_modified, creat, mod);

    			((TextView) view).setText(text);
    			return true;
    		}

    		if (view instanceof TextView && view.getId() == R.id.gameplay) {
				String clazz = cursor.getString(columnIndex); // Columns.GAMEPLAY
				clazz = formatGameplay(clazz);
				String name = clazz.equals(Bijoux.class.getSimpleName()) ? getResources().getString(R.string.title_bijoux)
						: getResources().getString(R.string.title_bouchebee);
    			((TextView) view).setText(name);
    			return true;
    		}

    		if (view instanceof ImageView && view.getId() == R.id.image) {
				String state = cursor.getString(columnIndex); // Columns.STATE
				String clazz = cursor.getString(cursor.getColumnIndexOrThrow(Columns.GAMEPLAY));

				Bitmap b = mBitmapCache.get(clazz + state);
				if (b == null) {
					if (mLocalAsqareContext == null) {
					    mLocalAsqareContext = new AsqareContext(GameListActivity.this);
					}
					Gameplay g = mLocalAsqareContext.createGameplay(clazz, state);

					b = g.createPreview(state, 256, 256);
					if (b != null) mBitmapCache.put(clazz + state, b);
				}

				if (b != null) ((ImageView) view).setImageBitmap(b);

				return true;
    		}

    		return false;
		}
	}
}
