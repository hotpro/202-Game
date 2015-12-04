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


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

//-----------------------------------------------

/**
 * A custom derived view.
 * <p/>
 * A custom version is needed to handle some events such as onMotionEvent
 * and to implement size restriction via onMeasure.
 */
public class CustomView extends View implements OnClickListener {

	private IUiEventListener mUiEventListener;

	/**
	 * Constructs a new CustomSurfaceView from info in a resource layout XML file.
	 */
	public CustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setClickable(true);
		setOnClickListener(this);
		setDrawingCacheEnabled(false);
	}

	public void setUiEventHandler(IUiEventListener listener) {
		mUiEventListener = listener;
	}

	/**
	 * Implements size restriction.
	 * <p/>
	 * TODO: The goal is to ensure the view is square. Maybe later.
	 * Right now we don't use it.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	/**
	 * Implements the method to handle touch motion events.
	 * <p/>
	 * @param event The motion event.
	 * @return True if the event was handled by the method.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mUiEventListener != null &&
				mUiEventListener.onTouchEvent(event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
      if (mUiEventListener != null &&
            mUiEventListener.onTrackballEvent(event)) {
          return true;
      }
      return super.onTrackballEvent(event);
    }

	/**
	 * Implements the method to handle a click event.
	 */
	@Override
    public void onClick(View view) {
		if (mUiEventListener != null) {
			mUiEventListener.onClick(view);
		}
	}

	/**
	 * Called when a key down event has occurred.
	 * <p/>
	 * This base implementation does nothing and returns false.
	 *
	 * @param keyCode The value in event.getKeyCode().
	 * @param event Description of the key event.
	 * @return If you handled the event, return true.
	 *         If you want to allow the event to be handled by the next receiver, return false.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mUiEventListener != null &&
				mUiEventListener.onKeyDown(keyCode, event)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Called when multiple down/up pairs of the same key have occurred in a row.
	 * <p/>
	 * This base implementation does nothing and returns false.
	 *
	 * @param keyCode The value in event.getKeyCode().
	 * @param count Number of pairs as returned by event.getRepeatCount().
	 * @param event Description of the key event.
	 * @return If you handled the event, return true.
	 *         If you want to allow the event to be handled by the next receiver, return false.
	 */
	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		if (mUiEventListener != null &&
				mUiEventListener.onKeyMultiple(keyCode, repeatCount, event)) {
			return true;
		}
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}

	/**
	 * Called when a key up event has occurred.
	 * <p/>
	 * This base implementation does nothing and returns false.
	 *
	 * @param keyCode The value in event.getKeyCode().
	 * @param event Description of the key event.
	 * @return If you handled the event, return true.
	 *         If you want to allow the event to be handled by the next receiver, return false.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mUiEventListener != null &&
				mUiEventListener.onKeyUp(keyCode, event)) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
