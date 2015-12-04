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

package edu.sjsu.cmpe202.view;

import android.view.MotionEvent;
import android.view.KeyEvent.Callback;
import android.view.View.OnClickListener;

//-----------------------------------------------

/**
 * Interface definition for callback invoked to handle user input:
 * - onTouchEvent from the View
 * - onClick from the View.OnClickListener
 * - onKeyDown/Up/Multiple from View.KeyEvent.Callback
 */
public interface IUiEventListener extends OnClickListener, Callback {

	/**
	 * Implements the method to handle touch motion events.
	 * <p/>
	 * @param event The motion event.
	 * @return True if the event was handled by the method.
	 */
	public boolean onTouchEvent(MotionEvent event);

    public boolean onTrackballEvent(MotionEvent event);
}
