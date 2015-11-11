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

package com.alfray.asqare;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	@SuppressWarnings("unused")
    private static final String TAG = "AboutActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.about);
	    setTitle(R.string.about_title);
	    WebView wv = (WebView) findViewById(R.id.web);

	    wv.loadUrl("file:///android_asset/about.html");
	    wv.setFocusable(true);
	    wv.setFocusableInTouchMode(true);
        wv.requestFocus();
	}
}
