package edu.sjsu.cmpe202.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public interface ILoginStateMachine {



    void backspace() ;
    void number( String digit ) ;
    void validPin() ;
    void invalidPin() ;

    void setStateNoPinDigits() ;
    void setStateOnePinDigit( String digit ) ;
    void setStateTwoPinDigits( String digit ) ;
    void setStateThreePinDigits( String digit ) ;
    void setStateFourPinDigits( String digit ) ;
    boolean isAuthenticated();
    void notifyObservers();
    void registerObservers(Myobserver myobserver);
}
