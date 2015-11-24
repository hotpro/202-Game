package com.alfray.asqare.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public class LoginStateFourPin implements ILoginState{

    ILoginStateMachine stateMachine;

    public LoginStateFourPin(ILoginStateMachine machine) {
        this.stateMachine = machine;
    }

    @Override
    public void backspace() {
        this.stateMachine.setStateThreePinDigits(null);
    }

    @Override
    public void number(String digit) {

    }

    @Override
    public void validPin() {
        this.stateMachine.notifyObservers();

    }

    @Override
    public void invalidPin() {
        this.stateMachine.setStateNoPinDigits();
    }
}
