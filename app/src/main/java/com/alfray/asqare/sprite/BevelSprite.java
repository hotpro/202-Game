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

import java.lang.reflect.Constructor;

import com.alfray.asqare.engine.Color;


/**
 * A sprite that can draw itself with a given color in a given rectangle.
 */
public abstract class BevelSprite extends Sprite {

    private final Color mColor;

    /**
     * Constructs a new BevelSprite with the specific color
     *
     * @param color The default color of the sprite.
     */
	protected BevelSprite(Color color) {
		super();
        mColor = color;
	}

    /**
     * Returns the default color of the sprite.
     */
    public Color getColor() {
        return mColor;
    }

	public static Sprite instantiate(Class<? extends Sprite> clazz,
			Color color, int inset, int border) {

		try {
			Constructor<? extends Sprite> constructor;
			constructor = clazz.getDeclaredConstructor(new Class<?>[] {
					Color.class, int.class, int.class });
			return constructor.newInstance(new Object[] {
					color, Integer.valueOf(inset), Integer.valueOf(border) });
		} catch (Exception e) {
			throw new RuntimeException(
					"Failed to instantiate BevelSprite of class " + clazz.getSimpleName() +
					" and color " + color.name(), e);
		}
	}

}
