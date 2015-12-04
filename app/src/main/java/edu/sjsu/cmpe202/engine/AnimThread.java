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

package edu.sjsu.cmpe202.engine;

import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import edu.sjsu.cmpe202.AsqareContext;
import edu.sjsu.cmpe202.engine.Board.Cell;


//-----------------------------------------------

public class AnimThread extends ActionThread {

    public static final int FPS = 20;
    public static final int MS_PER_FRAME = 1000/FPS;

    private static final String TAG = "Asqare.Anim";

    private static final boolean DEBUG = false;

	private long mCounter = 0;
	private AtomicInteger mAnimPending;
	private CellRegion mApplyDirty;
	private CellRegion mStopDirty;
	Action[] mActionArray = { null };


	public AnimThread(AsqareContext asqareContext) {
		super("AnimThread", asqareContext);
		mAnimPending = new AtomicInteger(0);
		mApplyDirty = new CellRegion(asqareContext);
		mStopDirty = new CellRegion(asqareContext);
        this.setPriority(Thread.currentThread().getPriority()+1);
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
	 * Resets the animation inner state.
	 */
	@Override
	public synchronized void clear() {
		super.clear();
	}

	// -----------------

	@SuppressWarnings("unused")
    @Override
    protected void runIteration() {
		try {
	    	long start = System.currentTimeMillis();

	    	doStuff();

	    	long now = System.currentTimeMillis() - start;
	    	now = MS_PER_FRAME - now;
	    	if (now > 0) {
	    		//--DEBUG-- Log.d(TAG, String.format("Wait-For: %d ms", now));
				waitFor(now);
	    	} else if (DEBUG && now < 0) {
	    		Log.d(TAG, String.format("Overflow: %d ms", now));
	        }
		} catch(Exception e) {
			Log.e(TAG, "AnimThread Loop Exception!", e);
		}
	}

	private void doStuff() {
    	long counter = ++mCounter;

		Board<? extends Cell> board = mAsqareContext.getBoard();
		if (board == null) return;

		Cell cell = null;
		AnimType type = null;
		AnimData data = null;

		CellRegion dirty = mApplyDirty;

		boolean no_pending_anim = true;
		int index = 0;
		while (mAnimPending.get() > 0 && mContinue && !mIsPaused) {
			synchronized(this) {
				cell = board.getCell(index++);
				if (cell == null) break;
			}

			synchronized(cell) {
				type = cell.getAnimType();
				if (type == null) continue;
				data = (AnimData) cell.getAnimData();
				if (data == null) continue;

				type.apply(this, cell, data, counter, dirty);
				if (no_pending_anim && !type.isInfinite()) no_pending_anim = false;
			}
    	}

		if (!dirty.isEmpty) {
			mAsqareContext.invalidateRegion(dirty);
		}

		if (no_pending_anim) {
			super.getNext(mActionArray);

    		Action action = mActionArray[0];

    		if (action != null) {
    			action.mOpcode.exec(mAsqareContext, action);
    		}
		}
    }

	//---------------

	/** The type of animation associated with a cell */
	public static enum AnimType {
		/** Blinks the sprite on and off at the repeat interval */
		BLINKING {
			@Override
			public boolean isInfinite() {
				return true;
			}

			@Override
			public void apply(AnimThread anim, Cell cell, AnimData data, long counter, CellRegion dirty) {
				long next = data.mNext;
				if (counter >= next) {
					data.mNext = next + data.mDelta;
					cell.setVisible(!cell.isVisible());
					dirty.add(cell);
				}
			}

			@Override
			public void onStop(AnimThread anim, Cell cell, AnimData data, CellRegion dirty) {
				super.onStop(anim, cell, data, dirty);
				if (!cell.isVisible()) {
					cell.setVisible(true);
					dirty.add(cell);
					cell.setAnimType(null);
				}
			}
		},

		/** Shrink animation based on inset of the containing sprite rectangle.
		 *  Once shrank completed, the sprite is made invisible. */
		SHRINK {
			@Override
			public void apply(AnimThread anim, Cell cell, AnimData data, long counter, CellRegion dirty) {
				if (!cell.isVisible()) {
					return;
				}

				long next = data.mNext;
				long n = counter - next;
				if (n > 0) {
					int i = data.mNumFrames - (int)n;
					if (i < 0) {
						onStop(anim, cell, data, dirty);
						return;
					}

					data.mNumFrames = i;
					data.mNext = counter;

					int d = data.mDelta * (int)n;
					i = cell.getShrinkInset();
					cell.setShrinkInset(i + d);
					dirty.add(cell);
				}
			}

			@Override
			public void onStop(AnimThread anim, Cell cell, AnimData data, CellRegion dirty) {
				super.onStop(anim, cell, data, dirty);
				cell.setVisible(false);
				cell.setShrinkInset(0);
				dirty.add(cell);
			}
		},

		/** Fly-over animation based on modifying the offset of the sprite at
		 * each frame. */
		FLYOVER {
			@Override
			public void apply(AnimThread anim, Cell cell, AnimData data, long counter, CellRegion dirty) {
				if (!cell.isVisible()) {
					return;
				}

				long next = data.mNext;
				long n = counter - next;
				if (n > 0) {
					int i = data.mNumFrames - (int)n;
					if (i < 0) {
						onStop(anim, cell, data, dirty);
						return;
					}

					data.mNumFrames = i;
					data.mNext = counter;
					i = (int)n;

					int dx = data.mDelta;
					int dy = dx >> 16;
					dx = dx & 0x0FFFF;

					int sx = data.mCurrent;
					int sy = sx >> 16;
					sx = sx & 0x0FFFF;

					sx += dx * i;
					sy += dy * i;
					data.mCurrent = (sy << 16) | sx;

					dirty.add(cell);
					cell.setOffset(sx, sy);
					dirty.add(cell);
				}
			}

			@Override
			public void onStop(AnimThread anim, Cell cell, AnimData data, CellRegion dirty) {
				super.onStop(anim, cell, data, dirty);
				dirty.add(cell);
				cell.setOffset(0, 0);
				dirty.add(cell);
			}
		};

		/**
		 * Applies the animation. This is not synchronized.
		 * @return The next time_ms to execute or 0 if the anim is completed.
		 */
		public abstract void apply(AnimThread anim, Cell cell, AnimData data, long counter, CellRegion dirty);

		/**
		 * Called when the animation is going to be stopped. The anim type
		 * should put the cell in the desired final state.
		 */
		public void onStop(AnimThread anim, Cell cell, AnimData data, CellRegion dirty) {
			cell.setAnimType(null);
			data.clear();
			anim.mAnimPending.decrementAndGet();

			if (DEBUG) anim.mAsqareContext.setStatus("anims: " + anim.mAnimPending.toString());
		}

		/** Indicates if this type never ends by itself. Default is false. */
		public boolean isInfinite() {
			return false;
		}
	}

	private static class AnimData {
		@SuppressWarnings("unused") public long mLong;
		public long mNext;
		public int mDelta;
		public int mCurrent;
		public int mNumFrames;

		/** Removes object references to prevent GC leaks */
		public void clear() {
		}
	}

	/**
	 * Stops the animation associated with a given cell.
	 *
	 * @param cell The cell to stop animate
	 */
	public void stopAnim(Cell cell) {
		if (cell == null) return;

		CellRegion dirty = mStopDirty;
		mStopDirty.clear();

		synchronized(cell) {
			AnimType type = cell.getAnimType();
			if (type != null) {
				AnimData data = (AnimData) cell.getAnimData();
				type.onStop(this, cell, data, dirty);
				cell.setAnimType(null);
				if (data != null) data.clear();
			}
		}

		if (!dirty.isEmpty) {
			mAsqareContext.invalidateRegion(dirty);
		}
	}

	/** Prepare the cell for a new animation. Stop any current animation,
	 *  setup the data if it doesn't exists or clear the existing one. */
	private AnimData beginPrepare(Cell cell) {
		stopAnim(cell);

		AnimData data = null;
		boolean need_clear = false;
		synchronized(cell) {
			if (cell.getAnimType() != null) {
				cell.setAnimType(null);
				need_clear = true;
			}
			data = (AnimData) cell.getAnimData();
		}

		if (data == null) {
			data = new AnimData();
		} else if (need_clear) {
			data.clear();
		}
		return data;
	}

	/** Caller MUST be synchronized on cell */
	private void endPrepare(Cell cell, AnimType type, AnimData data) {
		cell.setAnimType(type);
		cell.setAnimData(data);
		mAnimPending.incrementAndGet();
		wakeUp();

		if (DEBUG) mAsqareContext.setStatus("anims: " + mAnimPending.toString());
	}

	/**
	 * Starts a blinking animation with the corresponding repeat interval.
	 * When the animation completes, the sprite is visible.
	 *
	 * @param cell The cell to animate.
	 * @param numFps Blinking interval (1 on or 1 off) in frames, based on AnimThread.FPS (~10 fps).
	 */
	public void startBlinking(Cell cell, int numFrames) {
		if (cell == null || numFrames < 0) return;

		AnimData data = beginPrepare(cell);

		data.mNext = mCounter + numFrames;
		data.mDelta = numFrames;

		synchronized(cell) {
			cell.setVisible(false);
			endPrepare(cell, AnimType.BLINKING, data);
		}
	}

	/**
	 * Calls startBlinking on the first count cells of the given array.
	 * The whole thing is synchronized.
	 */
	public void startBlinking(Cell[] cells, int count, int numFrames) {
		synchronized(this) {
			for (int i = 0; i < count; i++) {
				startBlinking(cells[i], numFrames);
			}
		}
	}

	/**
	 * Starts a shrinking animation, based on changing the inset of the
	 * containing sprite rectangle. When the animation completes, the sprite
	 * is invisible.
	 *
	 * If insetPx or numFrames are zero, no animation happens.
	 *
	 * @param cell The cell to animate.
	 * @param insetPx The number of pixels to inset at each frame.
	 * @param numFrames The number of frames, based on AnimThread.FPS (~10 fps)
	 */
	public void startShrink(Cell cell, int insetPx, int numFrames) {
		if (cell == null || insetPx <= 0 || numFrames <= 0) return;

		AnimData data = beginPrepare(cell);

		data.mNext = mCounter;
		data.mDelta = insetPx;
		data.mNumFrames = numFrames;

		synchronized(cell) {
			cell.setShrinkInset(0);
			endPrepare(cell, AnimType.SHRINK, data);
		}
	}

	/**
	 * Calls startShrink on the first count cells of the given array.
	 * The whole thing is synchronized.
	 */
	public void startShrink(Cell[] cells, int count, int insetPx, int numFrames) {
		synchronized(this) {
			for (int i = 0; i < count; i++) {
				startShrink(cells[i], insetPx, numFrames);
			}
		}
	}

	/**
	 * Starts a fly-over animation, based on changing the position of the sprite
	 * by x/y pixels at each frame. The initial modified position is computed so
	 * that at the end of numFrames, the sprite achieves its real position, so
	 * it always flies from "away" to "home". When the animation completes the
	 * sprite is visible in its real position.
	 *
	 * If both xPxOffset and yPxOffset or numFrames are zero, no animation happens.
	 *
	 * @param cell The cell to animate.
	 * @param xPxOffset The number of X pixels to move at each frame.
	 * @param yPxOffset The number of X pixels to move at each frame.
	 * @param numFrames The number of frames, based on AnimThread.FPS (~10 fps)
	 */
	public void startFlyOver(Cell cell, int xPxOffset, int yPxOffset, int numFrames) {
		if (cell == null || numFrames <= 0) return;
		if (xPxOffset == 0 && yPxOffset == 0) return;

		AnimData data = beginPrepare(cell);

		if (xPxOffset < 0) xPxOffset = -xPxOffset;
		if (yPxOffset < 0) yPxOffset = -yPxOffset;

		int sx = -1 * xPxOffset * numFrames;
		int sy = -1 * yPxOffset * numFrames;

		data.mNext = mCounter;
		data.mDelta = (yPxOffset << 16) | xPxOffset;
		data.mCurrent = (sy << 16) | sx;
		data.mNumFrames = numFrames;

		synchronized(cell) {
			cell.setOffset(sx, sy);
			endPrepare(cell, AnimType.FLYOVER, data);
		}
	}

}

