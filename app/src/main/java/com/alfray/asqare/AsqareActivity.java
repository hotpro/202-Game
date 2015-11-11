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

package com.alfray.asqare;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.alfray.asqare.engine.AnimThread;
import com.alfray.asqare.gamelist.GameListActivity;
import com.alfray.asqare.gamelist.GameListProvider;
import com.alfray.asqare.gameplay.Gameplay;
import com.alfray.asqare.prefs.PrefsActivity;
import com.alfray.asqare.view.AsqareView;

/**
 * Base Asqare activity, it can display an asqare board view and manage
 * it's save/restore logic.
 */
public class AsqareActivity extends Activity {

	private static final String TAG = "AsqareActivity";

    private AsqareContext mContext;

    protected static final int PREFS_UPDATED = 42;


    // --------------------------

    public AsqareContext getContext() {
		return mContext;
	}

    // --------------------------
    // Activity Cycle

	/**
	 * Called when the activity is first created.
	 * <p/>
	 * Derived classes must override and call this.
	 */
    @Override
    public void onCreate(Bundle inState) {
        super.onCreate(inState);

    	Log.v(TAG, this.getClass().getSimpleName() + "/" +
    			"onCreate " + (inState == null ? "no" : " with") + " state");

        setupWindowAndContent();
        setupContextAndGameplay(inState);
    }

    /**
     * Called by onCreate to set requested window features and
     * set the content view. Derived classes can override to use
     * a different layout.
     * <p/>
     * If another layout is used, it must define the ids expected
     * by setupContextAndGameplay.
     */
	protected void setupWindowAndContent() {
        setContentView(R.layout.gameplay);
	}

	/**
	 * Called by onCreate to prepare the AsqareContext and restore a gameplay
	 * from the inState bundle, if any.
	 *
	 * @param inState The Bundle passed to onCreate
	 */
	protected void setupContextAndGameplay(Bundle inState) {
		mContext = new AsqareContext(this);
		AsqareView board_view = (AsqareView) findViewById(R.id.surface);
		if (board_view != null) {
	        mContext.setBoardView(board_view);
	        board_view.setContext(mContext);
		}
		TextView status_view = (TextView) findViewById(R.id.status);
        if (status_view != null) {
        	mContext.setStatusView(status_view);
            status_view.setText(this.getResources().getText(R.string.welcome));
        }
        mContext.getPrefsValues().update(this);

        if (inState != null) {
        	String clazz_name = inState.getString("asqare.gameplay");
        	String state = inState.getString("asqare.state");
        	mContext.createGameplay(clazz_name, state);
        }
	}

    /**
     * Allows you to save away your current state, when your activity is being paused and another
     * one resuming to interact with the user.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	Log.v(TAG, this.getClass().getSimpleName() + "/" + "onSaveInstanceState");

    	saveToBundle(outState);
    }

	@Override
    protected void onStart() {
    	super.onStart();
    	Log.v(TAG, this.getClass().getSimpleName() + "/" + "onStart");

    	// Pass. Nothing here, it's all done in onResume.
    }

    /**
     * Called when the activity will start interacting with the user.
     * Followed by either onSaveInstanceState() if another activity is being resumed in front
     * of it, or onPause() if this activity is being finished.
     * <p/>
     * Derived classes should create/start their own gameplay first and only then must they
     * call the super.
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.v(TAG, this.getClass().getSimpleName() + "/" + "onResume");

		mContext.updateWindowTitle();
    	Gameplay g = mContext.getGameplay();
    	AnimThread a = mContext.getAnimThread();
		if (g != null) mContext.startGameplay();
		if (g != null) g.pause(false);
		if (a != null) a.start();
		if (a != null) a.pauseThread(false);
    }

    /**
     * Followed by either onResume() if the activity returns back to the front, or onStop() if
     * it becomes invisible to the user.
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.v(TAG, this.getClass().getSimpleName() + "/" + "onPause");

    	Gameplay g = mContext.getGameplay();
    	AnimThread a = mContext.getAnimThread();
		if (g != null) g.pause(true);
		if (a != null) a.pauseThread(true);

		// last chance to save to a database, if any
		saveToBundle(null /* outState */);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.v(TAG, this.getClass().getSimpleName() + "/" + "onDestroy");

    	Gameplay g = mContext.getGameplay();
    	AnimThread a = mContext.getAnimThread();
		if (g != null) g.stop();
		if (a != null) a.waitForStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == PREFS_UPDATED) {
    		mContext.getPrefsValues().update(this);
    		mContext.onPrefsUpdated();
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }

    // --------------------------
    // Options menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, R.string.main_menu, 0, R.string.main_menu).setIcon(R.drawable.blue4x4);
    	menu.add(0, R.string.settings,  0, R.string.settings).setIcon(R.drawable.prefs);
    	menu.add(0, R.string.about,     0, R.string.about).setIcon(R.drawable.about);
    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.string.main_menu:
            showListGames();
    		break;
    	case R.string.settings:
    		showSettings();
    		break;
    	case R.string.about:
    		showAbout();
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }

    // --------------------------
    // Helpers

	/**
     * Changes the activity title.
     * <p/>
     * Always formatted as "appname - title".
     * <p/>
     * This MUST be called from a UI thread.
     */
    @Override
	public void setTitle(CharSequence title) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(getResources().getText(R.string.app_name));
    	sb.append(" - ");
    	sb.append(title);
    	super.setTitle(sb.toString());
    }

    /**
     * Changes the status text message.
     * <p/>
     * It is safe to call this from a non-UI thread.
     */
    public void setStatus(final CharSequence charSequence) {
    	final TextView view = mContext.getStatusView();
    	if (view != null) {
	    	view.post(new Runnable() {
				@Override
                public void run() {
					view.setText(charSequence);
				}
	    	});
    	}
    }

	/**
	 * Launch a GameplayActivity to play a new game
	 * @param gameplay_class
	 */
	protected void newGame(Class<?> gameplay_class) {
		Uri new_uri = Uri.withAppendedPath(GameListProvider.NEW_URI, gameplay_class.getName());
		Intent intent = new Intent(Intent.ACTION_INSERT, new_uri);
		Log.d(TAG, "Start new game: " + intent.toString());
		startActivity(intent);
	}

	/**
	 * Launch a GameplayActivity to play an existing game
	 */
	protected void selectExistingGame(long id) {
		Uri uri = ContentUris.withAppendedId(GameListProvider.CONTENT_URI, id);
		Intent intent = new Intent(Intent.ACTION_EDIT, uri);
		Log.d(TAG, "Start existing game: " + intent.toString());
		startActivity(intent);
	}

	/**
	 * Delete an existing game
	 */
	protected void deleteExistingGame(long id) {
		Uri uri = ContentUris.withAppendedId(GameListProvider.CONTENT_URI, id);
		getContentResolver().delete(uri, null /* where */, null /* selectionArgs */);
	}

	protected void showListGames() {
		Intent intent = new Intent(this, GameListActivity.class);
		 startActivityForResult(intent, 0);
	}

	protected void showSettings() {
		Intent intent = new Intent(this, PrefsActivity.class);
		 startActivityForResult(intent, PREFS_UPDATED);
	}

	protected void showAbout() {
		Intent intent = new Intent(this, AboutActivity.class);
		 startActivityForResult(intent, 0);
	}

	/**
	 * Saves gameplay state into given bundle.
	 *
	 * @param outState The bundle to save into. Can be null.
	 * @return The internal gameplay state if any, or null.
	 */
	protected String saveToBundle(Bundle outState) {
		Gameplay g = mContext.getGameplay();
    	if (g != null) {

    		String state = g.saveState();
    		if (outState != null) {
	    		outState.putString("asqare.gameplay", g.getClass().getName());
	    		outState.putString("asqare.state", state);
    		}
    		return state;
    	}

    	return null;
	}
}
