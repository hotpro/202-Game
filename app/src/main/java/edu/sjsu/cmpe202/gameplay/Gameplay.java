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

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import edu.sjsu.cmpe202.AsqareContext;
import edu.sjsu.cmpe202.engine.ActionThread;
import edu.sjsu.cmpe202.engine.Board;
import edu.sjsu.cmpe202.engine.Board.Cell;
import edu.sjsu.cmpe202.prefs.PrefsValues;
import edu.sjsu.cmpe202.view.IUiEventListener;

import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------

public abstract class Gameplay implements IUiEventListener, Subject {

	private Board<? extends Cell> mBoard;
	private Point mTempPoint = new Point();
	private final AsqareContext mContext;

    protected int mMoves;
    protected int mScore;
    protected ScoreCalculator scoreCalculator;

	public Gameplay(AsqareContext context) {
		mContext = context;
	}

	public AsqareContext getContext() {
		return mContext;
	}

	public void setBoard(Board<? extends Cell> board) {
		mBoard = board;
        mContext.setBoard(board);
	}

	public Board<? extends Cell> getBoard() {
		return mBoard;
	}

	/**
	 * Returns the title for window for this gameplay or null if there's no title to display.
	 */
	public abstract String getName();

	/**
	 * Returns a consise summary of the current gameplay score.
	 * This will be displayed in the game list.
	 */
	public String getScoreSummary() {
		String msg = String.format("%d/%d", mMoves, mScore);
		return msg;
	}

    public int getMoves() {
        return mMoves;
    }

    public int getScore() {
        return mScore;
    }

    //--------------------------

	/**
	 * Called to recreate a gameplay from a previously saved state.
	 * <p/>
	 * Also called with null if a new gameplay is created without any initial
	 * state.
	 * <p/>
	 * Derived classes must implement this.
	 */
	public abstract void create(String state);

	/**
	 * Called when a new gameplay has been stopped and must start again.
	 * <p/>
	 * Must do nothing if the gameplay had already been started and not stopped.
	 * <p/>
	 * Derived classes must implement this.
	 */
	public abstract void start();

	/**
	 * Called when the gameplay should save its state, either because the
	 * activity may be destroyed at any point thereafter or because we want
	 * to save it to the games database.
	 * <p/>
	 * The returned string can be empty, but not null.
	 * <p/>
	 * The state will be used in create() later.
	 */
	public abstract String saveState();

	/**
	 * Called when a gameplay is paused because the activity is being
	 * paused. The activity may disappear soon after, stop or restart.
	 * <p/>
	 * Derived classes must implement this,
	 *
	 * @param pause True if gameplay is going to be paused, false if resumed.
	 */
	public abstract void pause(boolean pause);

	/**
	 * Called when a gameplay is stopped because it is going to be changed
	 * for some other gameplay.
	 * <p/>
	 * Derived classes must implement this,
	 */
	public abstract void stop();


	/**
	 * Callback invoked by ActionThread.queueGameplayEvent().
	 * <p/>
	 * Gameplays typically use that to implement some kind of time-delayed
	 * state machine in conjunction with {@link ActionThread}.
	 *
	 * @param eventId The event id passed to queueGameplayEvent.
	 */
	public abstract void onActionEvent(int eventId);

	/**
	 * Callback invoked when the user closed the preferences panel
	 * and the {@link PrefsValues} instance has been updated.
	 * <p/>
	 * Preferences values may not have actually changed.
	 *
	 * @param The same {@link PrefsValues} that can be found in {@link AsqareContext}.
	 */
	public abstract void onPrefsUpdated(PrefsValues prefs);

	/**
	 * Called to recreate a preview static bitmap for a gameplay from a previously saved state.
	 * <p/>
	 * Implementations can return null if no preview is available or of no interest or
	 * if the state is invalid.
	 * <p/>
	 * @param state The state to reuse.
	 * @param width Desired pixel width of the bitmap. Actual bitmap width can vary.
	 * @param height Desired pixel height of the bitmap. Actual bitmap height can vary.
	 * @return The new bitmap or null.
	 */
	public abstract Bitmap createPreview(String state, int width, int height);

	//--------------------------
	// Interface IUiEventListener

	/**
	 * Implements IUiEventListener.onTouchEvent to process a touch event.
	 * <p/>
	 * If the motion event is an action move or down, it gets the cell
	 * touched (if valid) and triggers onCursorMoved accordingly.
	 * Action up activates the selection.
	 */
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_MOVE ||
    			event.getAction() == MotionEvent.ACTION_DOWN) {
			float dx = event.getX();
			float dy = event.getY();

			if (getContext().getCellForPoint((int) dx, (int) dy, mTempPoint)) {
				onCursorMoved(mTempPoint.x, mTempPoint.y);
			}
    	} else if (event.getAction() == MotionEvent.ACTION_UP) {
			float dx = event.getX();
			float dy = event.getY();
			if (getContext().getCellForPoint((int) dx, (int) dy, mTempPoint)) {
				onActivate();
			}
    	}

		return true;
	}

	/**
	 * Implements IUiEventListener.onTouchEvent to process a trackball event.
	 * <p/>
	 * For a move, it gets the delta and triggers onCursorMovedDelta accordingly.
	 * Action up activates the selection.
	 */
	@Override
    public boolean onTrackballEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_MOVE) {
    		float dx = event.getX();
    		dx = Math.signum(dx);
    		float dy = event.getY();
    		dy = Math.signum(dy);
    		onCursorMovedDelta((int) dx, (int) dy);
    	} else if (event.getAction() == MotionEvent.ACTION_UP) {
    		onActivate();
    	}
		return false;
    }

	//--------------------------
    // Interface view.Callback

	/* ignored here */
	@Override
    public boolean onKeyDown(int code, android.view.KeyEvent event) {
    	return false;
    }

	/**
	 * Implements view.Callback.onKeyUp to process a d-pad event.
	 * <p/>
	 * For a move, it gets the delta and triggers onCursorMovedDelta accordingly.
	 * Action up activates the selection.
	 */
    @Override
    public boolean onKeyUp(int code, KeyEvent event) {
    	switch(code) {
    	case KeyEvent.KEYCODE_DPAD_UP:
    		onCursorMovedDelta( 0, -1);
    		return true;
    	case KeyEvent.KEYCODE_DPAD_DOWN:
    		onCursorMovedDelta( 0,  1);
    		return true;
    	case KeyEvent.KEYCODE_DPAD_LEFT:
    		onCursorMovedDelta(-1,  0);
    		return true;
    	case KeyEvent.KEYCODE_DPAD_RIGHT:
    		onCursorMovedDelta( 1,  0);
    		return true;
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    	case KeyEvent.KEYCODE_ENTER:
    		onActivate();
    		return true;
    	}
    	return false;
    }

    /* ignored here */
    @Override
    public boolean onKeyMultiple(int keydown, int keyup, android.view.KeyEvent event) {
    	return false;
    }

    /* ignored here */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

	//--------------------------
    // Interface OnClickListener

    /* ignored here */
    @Override
    public void onClick(View view) {
    }

	//--------------------------

    /* ignored here */
	protected void onCursorMoved(int x, int y) {
		// pass
	}

    /* ignored here */
    protected void onCursorMovedDelta(int dx, int dy) {
    	// pass
	}

    /* ignored here */
	protected void onActivate() {
    	// pass
	}


	// observer pattern start Yunlong Concrete Subject

	private List<Observer> observers = new ArrayList<>();
	@Override
	public void register(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void unregister(Observer observer) {
		observers.remove(observer);
	}

	@Override
	public void notifyObserver() {
		for (Observer o : observers) {
			o.update(mMoves, mScore);
		}
	}

    protected void updateMessage() {
        notifyObserver();

    }
}


