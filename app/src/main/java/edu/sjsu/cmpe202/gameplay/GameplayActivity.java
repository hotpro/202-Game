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

package edu.sjsu.cmpe202.gameplay;

import java.security.InvalidParameterException;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import edu.sjsu.cmpe202.AsqareActivity;
import edu.sjsu.cmpe202.AsqareContext;
import com.alfray.asqare.R;
import edu.sjsu.cmpe202.gamelist.Columns;
import edu.sjsu.cmpe202.timer.DefaultGameCountDown;
import edu.sjsu.cmpe202.timer.GameCountDown;
import edu.sjsu.cmpe202.timer.GameCountDownListener;

//-----------------------------------------------

public class GameplayActivity extends AsqareActivity {

	private static final String TAG = "GameplayActivity";
	private Cursor mCursor;
    private Uri mUri;
	private int mColState = -1;
	private int mColScore = -1;
	private int mColModified = -1;
    private ProgressBar progress;

    @Override
	public void onCreate(Bundle inState) {

    	Log.v(TAG, this.getClass().getSimpleName() + "/" +
    			"onCreate " + (inState == null ? "no" : " with") + " state");

		super.onCreate(inState);

		AsqareContext c = getContext();
    	Intent intent = getIntent();
    	String action = intent.getAction();
    	mUri = intent.getData();

    	if (Intent.ACTION_EDIT.equals(intent.getAction())) {
    		// Nothing to do, uri has already been loaded above

    	} else if (Intent.ACTION_INSERT.equals(intent.getAction())) {

    		String class_name = mUri.getLastPathSegment();
    		if (!c.validateGameplayClass(class_name)) {
        		throw new InvalidParameterException("Invalid Gameplay class " + class_name + " in action " + action + " on uri " + mUri.toString() );
    		}

        	ContentValues values = new ContentValues();
    		values.put(Columns.GAMEPLAY, class_name);

    		mUri = getContentResolver().insert(mUri, values);

            // The new entry was created, set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
    	} else {
    		throw new UnsupportedOperationException("Unsupported action " + action + " on uri " + mUri.toString() );
    	}

		String[] projection = {
				Columns._ID,
				Columns.STATE,
				Columns.SCORE,
				Columns.MODIFIED_MS,
				Columns.GAMEPLAY
				};
		mCursor = managedQuery(mUri,
		        projection,
		        null,  // selection
                null,  // selectionArgs
		        null); // sortOrder
        setupTimer();
	}

    private GameCountDown gameCountDown;
    private TextView countDownView;
	private void setupTimer() {
        int count = 60;
        this.progress = (ProgressBar) findViewById(R.id.progress);
        this.progress.setMax(count);

        this.countDownView = (TextView) findViewById(R.id.count_down);
        this.gameCountDown = new DefaultGameCountDown(count);
        this.gameCountDown.register(new GameCountDownListener() {
            @Override
            public void onTick(long second) {
				progress.incrementProgressBy(1);
                countDownView.setText("Remaining: " + second + " s");
            }

            @Override
            public void onFinish() {
                countDownView.setText("Remaining: 0 s");
                progress.incrementProgressBy(1);
                showResult();
//                finish();
            }
        });
		this.gameCountDown.start();
    }

    private void showResult() {
        new MaterialDialog.Builder(this)
                .title("Good Game")
                .content("Score is: " + getContext().getGameplay().getScore() + "\nTotal Move: "
                        + getContext().getGameplay().getMoves())
                .positiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .cancelable(false)
                .show();
    }

	@Override
	protected void setupWindowAndContent() {
		// request the GL mode and _then_ call the super, which will do a setContentView.
		// Disabled GL mode due to current rendering issue [RM 20080728].
		// requestWindowFeature(Window.FEATURE_OPENGL);
		super.setupWindowAndContent();
	}

	@Override
	protected void onResume() {
    	Log.v(TAG, this.getClass().getSimpleName() + "/" + "onResume");

    	AsqareContext c = getContext();
		if (mCursor != null && mCursor.moveToFirst()) {
			setupColumnIndexes();
			String state = mCursor.getString(mColState);
			String class_name = mCursor.getString(mCursor.getColumnIndexOrThrow(Columns.GAMEPLAY));
			c.createGameplay(class_name, state);
			c.startGameplay();
		}

		super.onResume();
	}

	@Override
	protected String saveToBundle(Bundle outState) {
    	Log.v(TAG, "saveToBundle");

    	String state = super.saveToBundle(outState);
		AsqareContext c = getContext();
		Gameplay g = c.getGameplay();
		if (g != null && state != null && mCursor != null && mCursor.moveToFirst()) {
		    ContentValues values = new ContentValues();
		    values.put(Columns.STATE, state);
		    values.put(Columns.SCORE, g.getScoreSummary());
		    values.put(Columns.MODIFIED_MS, System.currentTimeMillis());
		    getContentResolver().update(mUri, values, null /*where*/, null /*selectionArgs*/);
		}
		return state;
	}

	private void setupColumnIndexes() {
		if (mColState == -1) mColState = mCursor.getColumnIndexOrThrow(Columns.STATE);
		if (mColScore == -1) mColScore = mCursor.getColumnIndexOrThrow(Columns.SCORE);
		if (mColModified == -1) mColModified = mCursor.getColumnIndexOrThrow(Columns.MODIFIED_MS);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.gameCountDown.cancel();
	}
}


