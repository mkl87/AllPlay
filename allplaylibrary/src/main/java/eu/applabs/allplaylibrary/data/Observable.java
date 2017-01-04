package eu.applabs.allplaylibrary.data;

import java.util.*;
import java.util.Observer;

/**
 * Implementation of the Java Observable
 */
public class Observable extends java.util.Observable {

    protected ArrayList<Observer> mObserverList = new ArrayList<>();
    protected boolean mHasChanged = false;

    public Observable() {

    }

    @Override
    public synchronized void addObserver(Observer observer) {
        if(!mObserverList.contains(observer)) {
            mObserverList.add(observer);
        }
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        if(mObserverList.contains(observer)) {
            mObserverList.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        for(Observer observer : mObserverList) {
            observer.update(this, null);
        }
    }

    @Override
    public void notifyObservers(Object arg) {
        for(Observer observer : mObserverList) {
            observer.update(this, arg);
        }
    }

    @Override
    public synchronized void deleteObservers() {
        mObserverList.clear();
    }

    @Override
    protected synchronized void setChanged() {
        mHasChanged = true;
    }

    @Override
    protected synchronized void clearChanged() {
        mHasChanged = false;
    }

    @Override
    public synchronized boolean hasChanged() {
        return mHasChanged;
    }

    @Override
    public synchronized int countObservers() {
        return mObserverList.size();
    }
}
