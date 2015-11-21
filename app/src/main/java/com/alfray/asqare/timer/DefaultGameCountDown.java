package com.alfray.asqare.timer;

import android.os.CountDownTimer;

/**
 * Created by yutao on 11/20/15.
 */
public class DefaultGameCountDown implements GameCountDown {
    private GameCountDownListener listener;
    private CountDownTimer countDownTimer;

    public DefaultGameCountDown(long secondsInFuture) {
        this.countDownTimer = new CountDownTimer(secondsInFuture * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                DefaultGameCountDown.this.onTick(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                DefaultGameCountDown.this.onFinish();
            }
        };
    }

    @Override
    public void start() {
        countDownTimer.start();
    }

    @Override
    public void register(GameCountDownListener listener) {
        this.listener = listener;
    }

    @Override
    public void unregister(GameCountDownListener listener) {
        this.listener = null;
    }

    @Override
    public void onTick(long second) {
        if (this.listener != null) {
            this.listener.onTick(second);
        }
    }

    @Override
    public void onFinish() {
        if (this.listener != null) {
            this.listener.onFinish();
        }
    }
}
