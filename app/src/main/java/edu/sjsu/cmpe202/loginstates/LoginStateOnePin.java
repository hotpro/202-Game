package edu.sjsu.cmpe202.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public class LoginStateOnePin implements ILoginState{

    ILoginStateMachine stateMachine;

    public LoginStateOnePin(ILoginStateMachine machine) {
        this.stateMachine = machine;
    }
    @Override
    public void backspace() {
        this.stateMachine.setStateNoPinDigits();
    }

    @Override
    public void number(String digit) {
        this.stateMachine.setStateTwoPinDigits(digit);
    }

    @Override
    public void validPin() {

    }

    @Override
    public void invalidPin() {

    }
}
