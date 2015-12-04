package edu.sjsu.cmpe202.loginstates;

/**
 * Created by Michaelzhd on 11/23/15.
 */
public interface ISubject {
    public void registerObservers(Myobserver myobserver);
    public void notifyObservers();
    public void unRegisterObserver(Myobserver myobserver);
}
