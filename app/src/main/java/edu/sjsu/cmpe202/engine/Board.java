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

import java.lang.reflect.Array;

import android.util.Log;

import edu.sjsu.cmpe202.engine.AnimThread.AnimType;
import edu.sjsu.cmpe202.sprite.Sprite;


//-----------------------------------------------

public class Board<C extends Board.Cell> {

	private static String TAG = "Asqare.Board";

	private int mWidth;
	private int mHeight;
	/** Board Cells, in order for(y) { for(x) } */
	private C[] mBoard;

	private Sprite mBackground;
	private int mSelectX;
	private int mSelectY;
	private Sprite mSelectSprite;


	@SuppressWarnings("unchecked")
	public Board(int w, int h, Class<C> cellClazz) {
		mWidth = w;
		mHeight = h;
		// Note: you can't write "mBoard = new C[n];" in Java because generics are a compile-time artifact.
		int n = w*h;
		mBoard = (C[]) Array.newInstance(cellClazz, n);
		try {
			for (int j = 0, k = 0; j < h; j++) {
				for (int i = 0; i < w; i++, k++) {
					C c = cellClazz.newInstance();
					c.setXY(i, j);
					mBoard[k] = c;
				}
			}
		} catch (IllegalAccessException e) {
			Log.e(TAG, "Board can't access constructor of " + cellClazz.toString(), e);
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			Log.e(TAG, "Board can't instantiate " + cellClazz.toString(), e);
			throw new RuntimeException(e);
		}
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public Sprite getBackground() {
		return mBackground;
	}

	public void setBackground(Sprite background) {
		mBackground = background;
	}

	/** Sets the sprite for cell x/y and returns the Cell */
	public C setSprite(int x, int y, Sprite sprite) {
		C c = mBoard[y * mWidth + x];
		c.setSprite(sprite);
		return c;
	}

	/** Direct access to board Cells, in order for(y) { for(x) } */
	public C[] getBoardArray() {
		return mBoard;
	}

	/**
	 * Returns the cell at cell coordinates x/y.
	 * @throws IndexOutOfBoundsException if x/y are invalid.
	 */
	public C getCell(int x, int y) {
		return mBoard[y * mWidth + x];
	}

	/**
	 * Returns the cell at index n, independent of the geometry of the board.
	 * Returns null if n is out of bounds.
	 */
	public C getCell(int n) {
		if (n >= mBoard.length) return null;
		return mBoard[n];
	}

	public void setSelectedCell(int x, int y) {
		mSelectX = x;
		mSelectY = y;
	}

	public int getSelectX() {
		return mSelectX;
	}

	public int getSelectY() {
		return mSelectY;
	}

	public void setSelectSprite(Sprite sprite) {
		mSelectSprite = sprite;
	}

	public Sprite getSelectSprite() {
		return mSelectSprite;
	}

	//--------------

	public static class Cell {
		private int mX;
		private int mY;
		private Sprite mSprite;
		private Sprite mBgSprite;
		private boolean mVisible = true;
		private AnimType mAnimType;
		private Object mAnimData;
		private int mShrinkInset;
		private int mOffsetX;
		private int mOffsetY;

		public Cell() {
		}

		/**
		 * Transfers this cell to destination cell.
		 * This is just a bit-copy of fields. Gameplays can override to
		 * add an animation.
		 * The source cell is cleared after.
		 *
		 * @param dest Destination cell to copy to.
		 * @param animThread Animation thread. If not null, stopAnim is
		 * called on the destination cell before swapping anim data.
		 */
		public void transferTo(Cell dest, AnimThread animThread, boolean visble) {

			animThread.stopAnim(dest);

			synchronized(this) {
				synchronized(dest) {
					// x/y do not change
					dest.mSprite = this.mSprite;
					dest.mBgSprite = this.mBgSprite;
					dest.mVisible = this.mVisible;
					dest.mShrinkInset = this.mShrinkInset;
					dest.mAnimType = this.mAnimType;
					// anim data is swapped to avoid a reallocation
					Object temp = dest.mAnimData;
					dest.mAnimData = this.mAnimData;
					this.mAnimData = temp;

					// clear this except x, y and anim data
					mSprite = null;
					mBgSprite = null;
					mVisible = visble;
					mShrinkInset = 0;
					mAnimType = null;
					mOffsetX = 0;
					mOffsetY = 0;
				}
			}
		}

		public int getX() {
			return mX;
		}

		public int getY() {
			return mY;
		}

		public void setXY(int x, int y) {
			mX = x;
			mY = y;
		}

		/**
		 * Sets sprite to display over the background. Can be null,
		 * @return self for chaining
		 */
		public Cell setSprite(Sprite sprite) {
			mSprite = sprite;
			return this;
		}

		/** Returns sprite to display over the background. Can be null, */
		public Sprite getSprite() {
			return mSprite;
		}

		/**
		 * Sets background sprite. Can be null.
		 * @return
		 * @return self for chaining
		 */
		public Cell setBackground(Sprite backgroundSprite) {
			mBgSprite = backgroundSprite;
			return this;
		}

		/** Returns background sprite. Can be null, */
		public Sprite getBackground() {
			return mBgSprite;
		}

		/**
		 * Sets if this cell is visible.
		 * @return self for chaining
		 */
		public Cell setVisible(boolean visible) {
			mVisible = visible;
			return this;
		}

		/** Indicates if this cell is visible */
		public boolean isVisible() {
			return mVisible;
		}

		public void setAnimType(AnimType animType) {
			mAnimType = animType;
		}

		public AnimType getAnimType() {
			return mAnimType;
		}

		public Object getAnimData() {
			return mAnimData;
		}

		public void setAnimData(Object animData) {
			mAnimData = animData;
		}

		public void setShrinkInset(int shrinkInset) {
			mShrinkInset = shrinkInset;
		}

		public int getShrinkInset() {
			return mShrinkInset;
		}

		public void setOffset(int px, int py) {
			mOffsetX = px;
			mOffsetY = py;
		}

		public int getOffsetX() {
			return mOffsetX;
		}

		public int getOffsetY() {
			return mOffsetY;
		}
	}
}


