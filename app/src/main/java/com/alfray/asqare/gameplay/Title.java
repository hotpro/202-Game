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

package com.alfray.asqare.gameplay;

import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.alfray.asqare.AsqareContext;
import com.alfray.asqare.R;
import com.alfray.asqare.engine.AnimThread;
import com.alfray.asqare.engine.Board;
import com.alfray.asqare.engine.Color;
import com.alfray.asqare.engine.Board.Cell;
import com.alfray.asqare.prefs.PrefsValues;
import com.alfray.asqare.sprite.BevelRect;
import com.alfray.asqare.sprite.BevelSquare;
import com.alfray.asqare.sprite.Sprite;

//-----------------------------------------------

public class Title extends Gameplay {

	private AnimThread mAnimThread;
	private boolean mStopped;
	private Board<Board.Cell> mBoard;
	private int mBx;
	private int mBy;
	private Sprite mYellow;
	private Sprite mGreen;
	private Sprite mBlue;
	private boolean mCreated;

	public Title(AsqareContext context) {
		super(context);

		mStopped = true; // stopped when created, start() must be called once
		mAnimThread = context.getAnimThread();
	}

	@Override
	public String getName() {
		return null; // don't alter title
	}

	@Override
	public void create(String state /* ignored */) {
		if (!mCreated) {
			mBlue = new BevelRect(Color.BLUE1, 2, 3);
			mGreen = new BevelSquare(Color.GREEN1, 8, 5);
			mYellow = new BevelSquare(Color.YELLOW1, 8, 5);
			mCreated = true;
		}
	}

	/** This implementation does not create previews. */
	@Override
	public Bitmap createPreview(String state, int width, int height) {
		return null;
	}

	@Override
	public String saveState() {
		// no state to save here
		return "";
	}

	@Override
	public void start() {
		if (!mStopped) return;
		mStopped = false;

		mAnimThread.start();

		if (mBoard == null) {
			mBx = 6;
			mBy = 8;
			mBoard = new Board<Board.Cell>(mBx, mBy, Board.Cell.class);
	        setBoard(mBoard);
	        setup();
		}
	}

	@Override
	public void pause(boolean pause) {
		mAnimThread.pauseThread(pause);
	}

	@Override
	public void stop() {
		mStopped = true;
		mAnimThread.clear();
		mBoard = null;
		setBoard(null);
	}

	@Override
	public String getScoreSummary() {
		return "N/A";
	}

	//-----------------


	@Override
	public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		onActivate();
    		return true;
    	}

		return false;
	}

    @Override
	public boolean onTrackballEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		onActivate();
    		return true;
    	}
		return false;
    }

    @Override
    public boolean onKeyUp(int code, KeyEvent event) {
    	switch(code) {
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    	case KeyEvent.KEYCODE_ENTER:
    		onActivate();
    		return true;
    	}
    	return super.onKeyUp(code, event);
    }


	//-----------------

	@Override
	protected void onActivate() {
		activateGame(null /* restoredState */);
	}

	private void activateGame(String restoredState) {
		// pass, do nothing
		//--getContext().vibrateTouch();
		//--getContext().startGameplay(Bijoux.class, restoredState, restoredState == null /* restart */);
	}

	@Override
	public void onActionEvent(int eventId) {
		if (eventId == 0 || eventId == 1) setMessage(eventId);
	}

	@Override
	public void onPrefsUpdated(PrefsValues prefs) {
		// pass. Nothing to do.
	}

	//-----------------

	private void setup() {
		setMessage(0);

		int bx = mBx;
		int by = mBy;
		for (int y = by - 1; y >= 0; --y) {
			for (int x = 0; x < bx; ++x) {
				createCell(x, y, 0, null, mBlue);
			}
		}

		int[] pos = { 1,6,		1,5,		1,4,	1,3,
					  2,2,		3,2,
					  4,3,		4,4,		4,5,	4,6,
					  2,4,		3,4,
					  };
		long time_ms = System.currentTimeMillis();
		for (int i = 0; i < pos.length; time_ms += 300) {
			int x = pos[i++];
			int y = pos[i++];
			createCell(x, y, time_ms, mYellow, null);
			createCell(x, y, time_ms + 100, mGreen, null);
		}
	}

	private void setMessage(final int i) {
		int id = i == 0 ? R.string.welcome : R.string.click_to_start;
		getContext().setStatus(getContext().getActivity().getResources().getText(id));

		if (!mStopped) {
			mAnimThread.queueDelayFor(2000 /* ms */);
			mAnimThread.queueGameplayEvent(this, 1 - i);
		}
	}

	private void createCell(final int x, final int y, long time_ms,
			final Sprite sprite, final Sprite background) {
		Cell cell = mBoard.getCell(x, y);
		if (time_ms > 0) {
			mAnimThread.queueWaitTime(time_ms);
			mAnimThread.queueSetBgSprite(cell, background);
			mAnimThread.queueSetSprite(cell, sprite);
		} else {
			cell.setBackground(background);
			cell.setSprite(sprite);
		}
	}
}


