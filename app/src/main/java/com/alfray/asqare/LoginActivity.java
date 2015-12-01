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
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alfray.asqare.gamelist.GameListActivity;
import com.alfray.asqare.loginstates.ILoginStateMachine;
import com.alfray.asqare.loginstates.LoginStateMachine;
import com.alfray.asqare.loginstates.Myobserver;

import it.anddev.tutorial.BasicOnKeyboardActionListener;
import it.anddev.tutorial.CustomKeyboardView;

public class LoginActivity extends Activity implements Myobserver {

	@SuppressWarnings("unused")
    private static final String TAG = "LoginActivity";

    private ILoginStateMachine loginStateMachine;
//    private LinearLayout pinTextViews;

    private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.login);
	    setTitle(R.string.login);

        mContext = this;

        TextView pinView1 = (TextView)findViewById(R.id.textView1);
        TextView pinView2 = (TextView)findViewById(R.id.textView2);
        TextView pinView3 = (TextView)findViewById(R.id.textView3);
        TextView pinView4 = (TextView)findViewById(R.id.textView4);
        loginStateMachine = new LoginStateMachine(pinView1,pinView2,pinView3,pinView4, mContext);
        loginStateMachine.registerObservers(this);
        if (this.loginStateMachine.isAuthenticated()) {
            Intent intent = new Intent(LoginActivity.this, GameListActivity.class);
            startActivity(intent);
            this.finish();
        }




        mKeyboard = new Keyboard(this, R.xml.keyboard);
        mKeyboardView = (CustomKeyboardView) findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView
                .setOnKeyboardActionListener(new BasicOnKeyboardActionListener(
                        this));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown keyCode: " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            this.loginStateMachine.validPin();
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            this.loginStateMachine.backspace();
        } else {
            this.loginStateMachine.number(String.valueOf(keyCode - KeyEvent.KEYCODE_0));
        }


        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode: " + keyCode);
//        if (keyCode == 3) {
//            return true;
//        }
        return super.onKeyUp(keyCode, event);
    }

    public void update() {
        if(this.loginStateMachine.isAuthenticated()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(LoginActivity.this, GameListActivity.class);
            startActivity(intent);
            this.finish();
        }
    }


}
