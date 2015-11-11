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

package com.alfray.asqare.sprite;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;

/**
 * A sprite that can draw itself.
 */
public abstract class Sprite {

	private Bitmap mBitmap;
	private Rect mTempRect = new Rect();

	/**
	 * Constructs a new Sprite.
	 */
	public Sprite() {
	}

	/**
	 * Called by the rendering view to draw this sprite in the desired
	 * rectangle.
	 * <p/>
	 * In the current implementation, the sprite will get cached in an
	 * intermediary bitmap the first time it is drawn. If the sprite is
	 * already cached and the bounds are identical, the bitmap is then reused.
	 *
	 * @param canvas The drawing canvas.
	 * @param drawRect The actual bounds where the sprite should render.
	 *                 This is never null. Some animations might resize the sprite,
	 *                 in which case this can change from frame to frame.
	 * @param originalRect Null most of the time, unless there's an animation that
	 *                 is altering the drawRect bounds, in which case this is the
	 *                 non-transformed bounds.
	 */
	public void draw(Canvas canvas, Rect drawRect, Rect originalRect) {
		boolean need_canvas = (mBitmap == null);
		if (originalRect == null) originalRect = drawRect;
		if (!need_canvas) {
			if (mBitmap.getWidth() != originalRect.width()) need_canvas = true;
			else if (mBitmap.getHeight() != originalRect.height()) need_canvas = true;
		}
		if (need_canvas) cacheBitmap(originalRect);

		canvas.drawBitmap(mBitmap, null /* originalRect */, drawRect, null /* paint */);
	}

	/**
	 * Draws the sprite in the specified canvas with the specified size.
	 * Actual sprite must implement this.
	 *
	 * @param canvas The drawing canvas.
	 * @param drawRect The actual bounds where the sprite should render.
	 *  The caller uses a temporary object, the draw method can alter the
	 *  rectangle.
	 */
	protected abstract void draw(Canvas canvas, Rect drawRect);

	private void cacheBitmap(Rect rect) {
		int w = rect.width();
		int h = rect.height();

		Bitmap b = Bitmap.createBitmap(w, h, Config.ARGB_4444);
		mBitmap = b;
		b.eraseColor(0 /* color 000000 */);

		Canvas c = new Canvas(b);

		mTempRect.set(0, 0, w, h);
		this.draw(c, mTempRect);
	}
}
