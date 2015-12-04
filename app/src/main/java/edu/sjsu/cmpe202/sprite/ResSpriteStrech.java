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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * A sprite based on an Android res/drawable resource, typically a PNG.
 */
public class ResSpriteStrech extends Sprite {

    private final Drawable mDrawable;
    private final int mInset;

    public ResSpriteStrech(int inset, Drawable drawable) {
        super();
        mInset = inset;
        mDrawable = drawable;
    }

    @Override
    protected void draw(Canvas canvas, Rect drawRect) {
        drawRect.inset(mInset, mInset);
        mDrawable.setBounds(drawRect);
        mDrawable.draw(canvas);
    }
}
