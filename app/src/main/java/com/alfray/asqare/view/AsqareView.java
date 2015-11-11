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

package com.alfray.asqare.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.alfray.asqare.AsqareContext;
import com.alfray.asqare.engine.Board;
import com.alfray.asqare.engine.CellRegion;
import com.alfray.asqare.engine.Board.Cell;
import com.alfray.asqare.sprite.Sprite;

public class AsqareView extends CustomView {

	private int mCellSx;
	private int mCellSy;
	private AsqareContext mAsqareContext;
	private drawBoard mDrawBoard;

	/**
	 * Constructs a new CustomSurfaceView from info in a resource layout XML file.
	 */
	public AsqareView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setContext(AsqareContext context) {
		mAsqareContext = context;
	}

	public int getCellSx() {
		return mCellSx;
	}

	public int getCellSy() {
		return mCellSy;
	}

	public void onBoardChanged() {
		setupSize(getWidth(), getHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mAsqareContext == null) return;

		Board<? extends Cell> b = mAsqareContext.getBoard();
		int sx = mCellSx;
		int sy = mCellSy;

		if (b != null && (sx <= 0 || sy <= 0)) {
			// This should not happen.
			setupSize(getWidth(), getHeight());
			sx = mCellSx;
			sy = mCellSy;
		}

		if (b != null && sx > 0 && sy > 0) {
			if (mDrawBoard == null) mDrawBoard = new drawBoard();
			mDrawBoard.draw(canvas, b, sx, sy);
		}
	}

	public static class drawBoard {

		private Rect mTempClipRect = new Rect();
		private Rect mTempCellRect = new Rect();
		private Rect mTempOrigRect = new Rect();
		private Cell[] mFlyOverCells;

		public void draw(Canvas canvas, Board<? extends Cell> b, int cellSx, int cellSy) {
			Rect clipRect = mTempClipRect;
			boolean useClipRect = canvas.getClipBounds(clipRect);
			Rect r = mTempCellRect;
			int bw = b.getWidth();
			int bh = b.getHeight();
			Sprite background = b.getBackground();
			Sprite select = b.getSelectSprite();
			int selx = b.getSelectX();
			int sely = b.getSelectY();
			Cell[] cells = b.getBoardArray();
			if (mFlyOverCells == null || mFlyOverCells.length < cells.length) {
				mFlyOverCells = new Cell[cells.length];
			}
			int numFlyOver = 0;

			int k = 0;
			for (int j = 0, ofy = 0; j < bh; ++j, ofy += cellSy) {
				for (int i = 0, ofx = 0; i < bw; ++i, ++k, ofx += cellSx) {
					r.set(ofx, ofy, ofx + cellSx, ofy + cellSy);
					if (!useClipRect || Rect.intersects(clipRect, r)) {
						Cell cell = cells[k];

						synchronized(cell) {
							Sprite t = cell.getBackground();
							if (t == null) t = background;
							if (t != null) t.draw(canvas, r, null);

							if (i == selx && j == sely && select != null)
								select.draw(canvas, r, null);

							t = cell.getSprite();
							if (t != null && cell.isVisible()) {
								// fly-overs are drawn later in a second pass
								if (cell.getOffsetX() != 0 || cell.getOffsetY() != 0) {
									mFlyOverCells[numFlyOver++] = cell;
									continue;
								}

								int n = cell.getShrinkInset();
								if (n > 0) {
									if (n+n < cellSx && n+n < cellSy) {
										mTempOrigRect.set(r);
										r.inset(n, n);
										t.draw(canvas, r, mTempOrigRect);
									}
								} else {
									t.draw(canvas, r, null);
								}
							}
						} // sync
					} // if intersects
				} // for i
			} // for j

			// deal with fly-over cells now. Cells are marked for this only
			// if already visible so no need to check again.
			for (k = 0; k < numFlyOver; k++) {
				Cell cell = mFlyOverCells[k];
				synchronized(cell) {
					mFlyOverCells[k] = null;

					int ofx = cell.getX() * cellSx + cell.getOffsetX();
					int ofy = cell.getY() * cellSy + cell.getOffsetY();
					r.set(ofx, ofy, ofx + cellSx, ofy + cellSy);

					Sprite t = cell.getSprite();
					t.draw(canvas, r, null);
				}
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setupSize(w, h);
	}

	private void setupSize(int w, int h) {
		if (mAsqareContext == null) return;

		mCellSx = mCellSy = 0;
		Board<? extends Cell> b = mAsqareContext.getBoard();
		if (b == null || w <= 0 || h <= 0) return;
		int bx = b.getWidth();
		int by = b.getHeight();
		if (bx <= 0 || by <= 0) return;
		mCellSx = w / bx;
		mCellSy = h / by;
	}

	public void invalidateCell(Cell cell) {
		int x = cell.getX();
		int y = cell.getY();
		int ox = mCellSx, sx = ox;
		int oy = mCellSy, sy = oy;
		ox *= x;
		oy *= y;
		int fx = ox + cell.getOffsetX();
		int fy = oy + cell.getOffsetY();
		if (fx != ox && fy != oy) {
			postInvalidate(fx, fy, fx + sx, fy + sy);
		}
		if (ox >= 0 && oy >= 0) {
			postInvalidate(ox, oy, ox + sx, oy + sy);
		}
	}

	/** mark the cell as being invalidated in the region */
	public void markForInvalidate(CellRegion region, Cell cell) {
		int x = cell.getX();
		int y = cell.getY();
		int ox = mCellSx, sx = ox;
		int oy = mCellSy, sy = oy;
		ox *= x;
		oy *= y;
		if (ox >= 0 && oy >= 0) {
			region.include(ox, oy, ox + sx, oy + sy);
		}
		int fx = cell.getOffsetX();
		int fy = cell.getOffsetY();
		if (fx != 0 && fy != 0) {
			ox += fx;
			oy += fy;
			region.include(ox, oy, ox + sx, oy + sy);
		}
	}

	public void invalidateRegion(CellRegion region) {
		if (!region.isEmpty) {
			postInvalidate(region.left, region.top, region.right, region.bottom);
		}
	}

	/**
	 * Returns the cell hit by the pixel at pixel_x/pixel_y.
	 * If valid, returns true and returns the cell x/y index in out_selection.
	 * If out of the game area, returns false and out_selection may or may not
	 * have been modified.
	 */
	public boolean getCellForPoint(int pixel_x, int pixel_y, Point out_selection) {
		int w = getWidth();
		int h = getHeight();
		if (w > 0 && h > 0 &&
				pixel_x > 0 && pixel_y > 0 &&
				pixel_x < w && pixel_y < h) {
			int sx = mCellSx;
			int sy = mCellSy;
			if (sx > 0 && sy > 0) {
				out_selection.x = sx = pixel_x / sx;
				out_selection.y = sy = pixel_y / sy;
				Board<? extends Cell> b = mAsqareContext.getBoard();
				return (sx >= 0 && sy >= 0 && sx < b.getWidth() && sy < b.getHeight());
			}
		}
		return false;
	}
}
