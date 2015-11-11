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

package com.alfray.asqare.engine;

import android.util.Log;

import com.alfray.asqare.AsqareContext;
import com.alfray.asqare.engine.Board.Cell;
import com.alfray.asqare.gameplay.Gameplay;
import com.alfray.asqare.sprite.Sprite;


//-----------------------------------------------

public abstract class ActionThread extends BaseThread {

    private static final String TAG = "Asqare.Action";
    private static final boolean DEBUG = false;

	// TODO make max number of actions resizable by the gameplay (but not dynamic)
	private Action[] mActions = new Action[250];

	/*
	 * mActions is a circular buffer.
	 * - first: first cell to execute. -1 if nothing to execute.
	 * - last: last cell to execute, included.
	 *
	 *
	 * push: -1 0 > 0 0 > 0 1 ... > 0 N > 0 99
	 * pop:  0 N > 1 N ... > N N > -1 0
	 *
	 * An overflow occurs when last==first after last++/loop.
	 */

	private int mFirst;
	private int mLast;


	public ActionThread(AsqareContext asqareContext) {
		super("ActionThread", asqareContext);
        init();
	}

	public ActionThread(String name, AsqareContext asqareContext) {
		super(name, asqareContext);
        init();
	}

	private void init() {
		this.setPriority(Thread.currentThread().getPriority()+1);

		for (int i = 0; i < mActions.length; i++) {
			mActions[i] = new Action();
			mActions[i].clear();
		}
		reset();
	}

    /**
     * Starts the thread if it wasn't already started.
     * Does nothing if started.
     */
    @Override
    public synchronized void start() {
    	super.start();
    }

	/**
	 * Resets the circular buffer of actions and also clears every single
	 * action to make sure no outside object reference is kept behind.
	 */
	@Override
	public synchronized void clear() {
		reset();
		for (int i = 0; i < mActions.length; i++) {
			mActions[i].clear();
		}
		// no need to wake up since there's nothing to execute
	}

	// -----------------

	protected long getNext(Action[] action_array) {
		long wait_for_ms = 0;
		synchronized(this) {
			Action action = peek_NoSync();
			action_array[0] = action;

			if (action != null) {
				boolean must_pop = true;
				if (action.mOpcode == Opcode.WAIT) {
					wait_for_ms = action.mValue - System.currentTimeMillis();
					must_pop = false;
				}

				if (must_pop || wait_for_ms <= 0) {
					pop_NoSync();
				}
			}
		}
		return wait_for_ms;
	}

	//---------------

	protected static class Action {
		Opcode mOpcode;
		public Cell mCell;
		public Sprite mSprite;
		public Gameplay mGameplay;
		public long mValue;

		public void clear() {
			mOpcode = null;
			mCell = null;
			mSprite = null;
			mGameplay = null;
			mValue = 0;
		}
	}

	protected enum Opcode {
		/** Parameters: time_ms */
		WAIT {
			@Override
			public void exec(AsqareContext context, Action a) {
				// handled in the main loop, nothing here
			}
		},

		/** Parameters: cell, sprite */
		SET_SPRITE {
			@Override
			public void exec(AsqareContext context, Action a) {
				if (DEBUG) Log.d(TAG, "Exec SetSprite");
				a.mCell.setSprite(a.mSprite);
				context.invalidateCell(a.mCell);
			}
		},

		/** Parameters: cell, sprite */
		SET_BG_SPRITE {
			@Override
			public void exec(AsqareContext context, Action a) {
				if (DEBUG) Log.d(TAG, "Exec Set-BG-Sprite");
				a.mCell.setBackground(a.mSprite);
				context.invalidateCell(a.mCell);
			}
		},

		/** Parameters: cell, bool state */
		SET_VISIBILITY {
			@Override
			public void exec(AsqareContext context, Action a) {
				if (DEBUG) Log.d(TAG, "Exec SetVisible");
				a.mCell.setVisible(a.mValue != 0);
				context.invalidateCell(a.mCell);
			}
		},

		/** Parameters: gameplay, event_id */
		GAMEPLAY_EVENT {
			@Override
			public void exec(AsqareContext context, Action a) {
				if (DEBUG) Log.d(TAG, "Exec GameplayEvent");
				a.mGameplay.onActionEvent((int)a.mValue);
			}
		};

		public abstract void exec(AsqareContext context, Action a);
	}

	/** Resets circular buffer but does not clear content. */
	private void reset() {
		mFirst = -1;
		mLast = 0;
	}

	/**
	 * Prepares a new Action to be queued in the circular buffer.
	 * Must be called from a synchronized method.
	 * @return The new Action to fill.
	 * @throws IndexOutOfBoundsException if there's a circular buffer overflow.
	 */
	private Action push_NoSync() {
		if (mFirst == -1) {
			mFirst = mLast = 0;
		} else {
			mLast++;
			if (mLast == mActions.length) mLast = 0;
			if (mLast == mFirst) {
				throw new IndexOutOfBoundsException("Circular buffer overflow");
			}
		}
		return mActions[mLast];
	}

	/**
	 * Returns the first action to execute but does not remove it from the buffer.
	 * Returns null if the queue is empty.
	 * Must be called from a synchronized method.
	 */
	private Action peek_NoSync() {
		if (mFirst == -1) return null;
		return mActions[mFirst];
	}

	/**
	 * Discards the first action, if any.
	 * Must be called from a synchronized method.
	 */
	private void pop_NoSync() {
		if (mFirst == -1) return;
		if (mFirst == mLast) {
			reset();
		} else {
			mFirst++;
			if (mFirst == mActions.length) mFirst = 0;
		}
	}

	/** Queues an action to wait for the given <em>absolute</em>
	 * time expressed in System.currentTimeMillis(). */
	public synchronized void queueWaitTime(long timeMs) {
		if (DEBUG) Log.d(TAG, "Queue Wait");
		Action a = push_NoSync();
		a.mOpcode = Opcode.WAIT;
		a.mValue = timeMs;
		wakeUp();
	}

	/** Queues an action to wait for the given <em>relative</em>
	 * time expressed related to System.currentTimeMillis(). */
	public synchronized void queueDelayFor(long delayMs) {
		if (DEBUG) Log.d(TAG, "Queue Wait");
		Action a = push_NoSync();
		a.mOpcode = Opcode.WAIT;
		a.mValue = System.currentTimeMillis() + delayMs;
		wakeUp();
	}

	/** Queues an action to change the sprite in the given cell. */
	public synchronized void queueSetSprite(Cell cell, Sprite sprite) {
		if (DEBUG) Log.d(TAG, "Queue SetSprite");
		Action a = push_NoSync();
		a.mOpcode = Opcode.SET_SPRITE;
		a.mCell = cell;
		a.mSprite = sprite;
		wakeUp();
	}

	/** Queues an action to change the background sprite in the given cell. */
	public synchronized void queueSetBgSprite(Cell cell, Sprite sprite) {
		if (DEBUG) Log.d(TAG, "Queue Set-BG-Sprite");
		Action a = push_NoSync();
		a.mOpcode = Opcode.SET_BG_SPRITE;
		a.mCell = cell;
		a.mSprite = sprite;
		wakeUp();
	}

	/** Queues an action to change the visibility of the given cell. */
	public synchronized void queueSetVisibility(Cell cell, boolean state) {
		if (DEBUG) Log.d(TAG, "Queue SetVisible");
		Action a = push_NoSync();
		a.mOpcode = Opcode.SET_VISIBILITY;
		a.mCell = cell;
		a.mValue = state ? 1 : 0;
		wakeUp();
	}

	/** Queues an action to generate a Gameplay.onActioEvent with the given event id. */
	public synchronized void queueGameplayEvent(Gameplay gameplay, int eventId) {
		if (DEBUG) Log.d(TAG, "Queue GameplayEvent");
		Action a = push_NoSync();
		a.mOpcode = Opcode.GAMEPLAY_EVENT;
		a.mGameplay = gameplay;
		a.mValue = eventId;
		wakeUp();
	}
}


