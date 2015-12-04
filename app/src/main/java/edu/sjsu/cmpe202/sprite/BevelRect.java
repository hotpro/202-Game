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

package edu.sjsu.cmpe202.sprite;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import edu.sjsu.cmpe202.engine.Color;

public class BevelRect extends BevelSprite {

	private final int mInset;
	private Paint fillPaint;
	private Paint upPaint;
	private Paint downPaint;
	private Rect  r = new Rect();

	public BevelRect(Color color, int inset, int border) {
		super(color);
		mInset = inset;

		fillPaint = getColor().getPaint();
		fillPaint.setStyle(Paint.Style.FILL);
		upPaint   = getColor().getPaint(0 + 0x00202020);
		upPaint.setStrokeWidth(border);
		upPaint.setStyle(Paint.Style.STROKE);
		downPaint = getColor().getPaint(0 - 0x00202020);
		downPaint.setStrokeWidth(border);
		downPaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void draw(Canvas canvas, Rect rect) {

		int cx = rect.centerX();
		int cy = rect.centerY();
		int w = rect.width() >> 1;
		int h = rect.height() >> 1;
		w -= mInset;
		h -= mInset;

		int left = cx - w, top = cy - h, right = cx + w, bottom = cy + h;
		r.set(left, top, right, bottom);

		canvas.drawRect(r, fillPaint);

		canvas.drawLine(left,  bottom, right, bottom, downPaint);
		canvas.drawLine(right, bottom, right, top,    downPaint);

		canvas.drawLine(left, bottom, left,  top, upPaint);
		canvas.drawLine(left, top,    right, top, upPaint);
	}
}
