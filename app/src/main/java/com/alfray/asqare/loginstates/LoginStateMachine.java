package com.alfray.asqare.loginstates;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public class LoginStateMachine implements ILoginStateMachine, ISubject {

    private String pin = "";
    private boolean authenticated = false;
    private int pinCount = 0;
    private LoginStateNoPin pin0;
    private LoginStateOnePin pin1;
    private LoginStateTwoPin pin2;
    private LoginStateThreePin pin3;
    private LoginStateFourPin pin4;
    private TextView pinView1;
    private TextView pinView2;
    private TextView pinView3;
    private TextView pinView4;

    private ILoginState state;
    private String digit1,digit2,digit3,digit4;

    private ArrayList<Myobserver> observers;
    public LoginStateMachine(TextView pinView1,TextView pinView2,TextView pinView3,TextView pinView4 ) {
        pin0 = new LoginStateNoPin(this);
        pin1 = new LoginStateOnePin(this);
        pin2 = new LoginStateTwoPin(this);
        pin3 = new LoginStateThreePin(this);
        pin4 = new LoginStateFourPin(this);
        this.state = pin0;

        this.pinView1 = pinView1;
        this.pinView2 = pinView2;
        this.pinView3 = pinView3;
        this.pinView4 = pinView4;
        this.observers = new ArrayList<Myobserver>();

    }

    @Override
    public void backspace() {
        this.state.backspace();
    }

    @Override
    public void number(String digit) {
        this.state.number(digit);
    }

    @Override
    public void validPin() {
        this.state.validPin();
    }

    @Override
    public void invalidPin() {
        this.state.invalidPin();
    }

    @Override
    public void setStateNoPinDigits() {
        this.pinCount = 0;
        this.state = pin0;
        this.digit1 = "";
        this.digit2 = "";
        this.digit3 = "";
        this.digit4 = "";
        this.pinView1.setText("");
        this.pinView2.setText("");
        this.pinView3.setText("");
        this.pinView4.setText("");
    }

    @Override
    public void setStateOnePinDigit(String digit) {
        this.pinCount = 1;
        this.state = pin1;
        if (digit != null) {
            this.digit1 = digit;
            this.pinView1.setText("*");
        } else {
            this.digit2 = "";
            this.pinView2.setText("");
        }

    }

    @Override
    public void setStateTwoPinDigits(String digit) {
        this.pinCount = 2;
        this.state = pin2;
        if (digit != null) {
            this.digit2 = digit;
            this.pinView2.setText("*");
        } else {
            this.digit3 = "";
            this.pinView3.setText("");
        }

    }

    @Override
    public void setStateThreePinDigits(String digit) {
        this.pinCount = 3;
        this.state = pin3;
        if (digit != null) {
            this.digit3 = digit;
            this.pinView3.setText("*");
        } else {
            this.digit3 = "";
            this.pinView4.setText("");

        }

    }

    @Override
    public void setStateFourPinDigits(String digit) {
        this.pinCount = 4;
        this.state = pin4;
        if (digit != null) {
            this.digit4 = digit;
            this.pinView4.setText("*");
            String inputPin = digit1 + digit2 + digit3 + digit4;
            if ("".equals(pin) || pin == null) {
                this.pin = inputPin;
                this.setStateNoPinDigits();

            } else if (pin.equals(inputPin)) {
                this.authenticated = true;
            } else {
                setStateNoPinDigits();
            }
        } else {
            this.pinView4.setText("");
        }


    }

    public boolean isAuthenticated(){
        return this.authenticated;
    }


    public void registerObservers(Myobserver myobserver) {
        this.observers.add(myobserver);
    }
    public void notifyObservers() {
        for(Myobserver observer : this.observers){
            observer.update();
        }
    }

    public void unRegisterObserver(Myobserver myobserver){
        this.observers.remove(myobserver);
    };


}
