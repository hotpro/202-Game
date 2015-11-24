package com.alfray.asqare.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public class LoginStateNoPin implements ILoginState {

    ILoginStateMachine stateMachine;

    public LoginStateNoPin(ILoginStateMachine machine){
        this.stateMachine = machine;
    }

    @Override
    public void backspace() {

    }

    @Override
    public void number(String digit) {
        this.stateMachine.setStateOnePinDigit(digit);
    }

    @Override
    public void validPin() {

    }

    @Override
    public void invalidPin() {

    }
}
