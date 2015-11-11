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

package com.alfray.asqare.gameplay;

import com.alfray.asqare.AsqareContext;

//-----------------------------------------------

/**
 * Crude list of available gameplays.
 * <p/>
 * Todo: use reflection to find these classes in this package.
 */
public class AvailableGameplays {

	private static final AvailableGameplays sSingleton = new AvailableGameplays();

	private static final Class<?>[] sGameplays = {
		Bijoux.class,
		BoucheBee.class
	};

	private AvailableGameplays() {
	}

	public static AvailableGameplays getInstance() {
		return sSingleton;
	}

	public Class<?>[] getAvailableGameplays() {
		return sGameplays;
	}

	@SuppressWarnings("unchecked")
	public String getDisplayName(AsqareContext context, Class<?> clazz) {
		try {
			Gameplay g = context.instantiateGameplay((Class<? extends Gameplay>) clazz);
			return g.getName();
		} catch (Throwable t) {
			// ignore
		}
		return clazz.getSimpleName();
	}

}


