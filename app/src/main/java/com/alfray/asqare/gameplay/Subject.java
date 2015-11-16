package com.alfray.asqare.gameplay;

/**
 * Created by yunlongxu on 11/16/15.
 */
public interface Subject {
    public void register(Observer observer);
    public void unregister(Observer observer);
    public void notifyObserver();
}
