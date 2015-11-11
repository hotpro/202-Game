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

package com.alfray.asqare.prefs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsValues {

	private boolean mPlayAnims;
	private boolean mVibrateTouch;
	private boolean mVibrateSequence;
    private String mVisualTheme;

	public PrefsValues() {
		reset();
	}

	public boolean playAnims() {
		return mPlayAnims;
	}

	public boolean vibrateTouch() {
		return mVibrateTouch;
	}

	public boolean vibrateSequence() {
		return mVibrateSequence;
	}

	public String getVisualTheme() {
        return mVisualTheme;
    }

	public void reset() {
		mPlayAnims = true;
		mVibrateTouch = true;
		mVibrateSequence = true;
		mVisualTheme = "default";
	}

	public void update(Activity activity) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		mPlayAnims = prefs.getBoolean("play_anims", true);
		mVibrateTouch = prefs.getBoolean("vib_touch", true);
		mVibrateSequence = prefs.getBoolean("vib_seq", true);
		mVisualTheme = prefs.getString("visual", "default");
	}
}
