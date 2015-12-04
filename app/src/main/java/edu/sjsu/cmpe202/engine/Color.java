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

import android.graphics.Paint;

public enum Color {
	RED		(0xFFFF0000),
	ORANGE	(0xFFFF9900),
	YELLOW	(0xFFFFFF00),
	GREEN	(0xFF00FF00),
	CYAN	(0xFF00FFFF),
	BLUE	(0xFF0000FF),
	PINK	(0xFFFF99FF),
	PURPLE	(0xFF9966FF),

	RED1	(0xFFDF2020),
	RED2	(0xFF992020),
	ORANGE1	(0xFFDF7920),
	YELLOW1	(0xFFDFDF20),
	GREEN1	(0xFF20DF20),
	CYAN1	(0xFF20DFDF),
	BLUE1	(0xFF2020DF),
	PINK1	(0xFFDF79DF),
	PURPLE1	(0xFF7946DF);

	public static final Color[] colors1 = {
		RED1, RED2, ORANGE1, YELLOW1, GREEN1, CYAN1, BLUE1, PINK1, PURPLE1
	};

	private final int mARGB;

	private Color(int argb) {
		mARGB = argb;
	}

	public int getRgb() {
		return mARGB;
	}

	public Paint getPaint() {
		Paint paint = new Paint();
		paint.setColor(mARGB);
		return paint;
	}

	public Paint getPaint(int rgb_delta) {
		int sign;
		if (rgb_delta > 0) {
			sign = 1;
		} else {
			sign = -1;
			rgb_delta = 0 - rgb_delta;
		}
		int rgb;
		rgb = compose(mARGB, rgb_delta, 16, sign);
		rgb = compose(  rgb, rgb_delta,  8, sign);
		rgb = compose(  rgb, rgb_delta,  0, sign);

		Paint paint = new Paint();
		paint.setColor(rgb);
		return paint;
	}

	private int compose(int rgb1, int rgb2, int offset, int sign) {
		 int a = ((rgb1 >> offset) & 0x00FF) + sign * ((rgb2 >> offset) & 0x00FF);
		 a = (a > 255 ? 255 : (a < 0 ? 0 : (a & 0x00FF)));
		 rgb1 = (rgb1 & ~(0x00FF << offset)) + (a << offset);
		 return rgb1;
	}
}
