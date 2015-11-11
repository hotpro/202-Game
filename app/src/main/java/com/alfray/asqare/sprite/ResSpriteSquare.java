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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * A sprite based on an Android res/drawable resource, typically a PNG.
 */
public class ResSpriteSquare extends Sprite {

    private final Drawable mDrawable;
    private Rect  r = new Rect();
    private final int mInset;

    public ResSpriteSquare(int inset, Drawable drawable) {
        super();
        mInset = inset;
        mDrawable = drawable;
    }

    @Override
    protected void draw(Canvas canvas, Rect rect) {
        int cx = rect.centerX();
        int cy = rect.centerY();
        int s = rect.width() >> 1;
        int h = rect.height() >> 1;
        if (s > h) s = h;
        s -= mInset;

        int left = cx - s, top = cy - s, right = cx + s, bottom = cy + s;
        r.set(left, top, right, bottom);

        mDrawable.setBounds(r);
        mDrawable.draw(canvas);
    }
}
