package edu.sjsu.cmpe202.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public class LoginStateTwoPin implements ILoginState {

    ILoginStateMachine stateMachine;

    public LoginStateTwoPin(ILoginStateMachine machine) {
        this.stateMachine = machine;
    }
    @Override
    public void backspace() {
        this.stateMachine.setStateOnePinDigit(null);
    }

    @Override
    public void number(String digit) {
        this.stateMachine.setStateThreePinDigits(digit);
    }

    @Override
    public void validPin() {

    }

    @Override
    public void invalidPin() {

    }
}
