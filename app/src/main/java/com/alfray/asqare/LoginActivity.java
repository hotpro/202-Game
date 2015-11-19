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
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alfray.asqare.gamelist.GameListActivity;

import it.anddev.tutorial.BasicOnKeyboardActionListener;
import it.anddev.tutorial.CustomKeyboardView;

public class LoginActivity extends Activity {

	@SuppressWarnings("unused")
    private static final String TAG = "LoginActivity";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.login);
	    setTitle(R.string.login);

//		KeyboardView kbdview = (KeyboardView)findViewById(R.id.passcodekbd);
//		TextView passcodeview = (TextView)findViewById(R.id.passcode);
//        passcodeview.setText("Passcode Shown Here");
        Button loginBtn = (Button)findViewById(R.id.login_button);
        loginBtn.setText("Press to login");
        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, GameListActivity.class);


                startActivity(intent);
            }
        });



//	    WebView wv = (WebView) findViewById(R.id.web);

//	    wv.loadUrl("file:///android_asset/about.html");
//	    wv.setFocusable(true);
//	    wv.setFocusableInTouchMode(true);
//        wv.requestFocus();

        mKeyboard = new Keyboard(this, R.xml.keyboard);
        mKeyboardView = (CustomKeyboardView) findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView
                .setOnKeyboardActionListener(new BasicOnKeyboardActionListener(
                        this) {
                    @Override
                    public void onKey(int primaryCode, int[] keyCodes) {
                        super.onKey(primaryCode, keyCodes);

                    }
                });
        showKeyboardWithAnimation();
	}


    private CustomKeyboardView mKeyboardView;
    private Keyboard mKeyboard;

    /***
     * Mostra la tastiera a schermo con una animazione di slide dal basso
     */
    private void showKeyboardWithAnimation() {
        if (mKeyboardView.getVisibility() == View.GONE) {
            Animation animation = AnimationUtils
                    .loadAnimation(LoginActivity.this,
                            R.anim.slide_in_bottom);
            mKeyboardView.showWithAnimation(animation);
        }
    }
}
