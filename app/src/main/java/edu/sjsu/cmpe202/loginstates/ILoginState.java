package edu.sjsu.cmpe202.loginstates;

/**
 * Created by Michaelzhd on 11/19/15.
 */
public interface ILoginState {
    void backspace() ;
    void number( String digit ) ;
    void validPin() ;
    void invalidPin() ;

}
