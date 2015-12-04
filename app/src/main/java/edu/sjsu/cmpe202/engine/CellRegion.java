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

import edu.sjsu.cmpe202.AsqareContext;
import edu.sjsu.cmpe202.engine.Board.Cell;


public class CellRegion {

	public int left, top, right, bottom;
	public boolean isEmpty;

	private final AsqareContext mAsqareContext;

	public CellRegion(AsqareContext asqareContext) {
		mAsqareContext = asqareContext;
	}

	public void clear() {
		isEmpty = true;
	}

	public void add(Cell cell) {
		mAsqareContext.getBoardView().markForInvalidate(this, cell);
	}

	public void include(int l, int t, int r, int b) {
		if (isEmpty) {
			left = l;
			right = t;
			top = r;
			bottom = b;
			isEmpty = false;
		} else {
			if (l < left) left = l;
			if (r > right) right = r;
			if (t < top) top = t;
			if (b > bottom) bottom = b;
		}
	}
}
