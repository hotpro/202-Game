package edu.sjsu.cmpe202.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public class LoginStateThreePin implements ILoginState{

    ILoginStateMachine stateMachine;

    public LoginStateThreePin(ILoginStateMachine machine) {
        this.stateMachine = machine;
    }


    @Override
    public void backspace() {
        this.stateMachine.setStateTwoPinDigits(null);
    }

    @Override
    public void number(String digit) {
        this.stateMachine.setStateFourPinDigits(digit);
    }

    @Override
    public void validPin() {

    }

    @Override
    public void invalidPin() {

    }
}
