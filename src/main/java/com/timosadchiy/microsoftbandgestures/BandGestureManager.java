package com.timosadchiy.microsoftbandgestures;

import com.microsoft.band.BandIOException;

public interface BandGestureManager {

    void setLeftHandOutside();

    void setLeftHandInside();

    void setRightHandOutside();

    void setRightHandInside();

    boolean registerPalmSideSwipeRightEventListener(PalmSideSwipeEventListener var1) throws BandIOException;

    void unregisterPalmSideSwipeRightEventListeners() throws BandIOException;

    void unregisterPalmSideSwipeRightEventListener(PalmSideSwipeEventListener var1) throws BandIOException;

    boolean registerPalmSideSwipeLeftEventListener(PalmSideSwipeEventListener var1) throws BandIOException;

    void unregisterPalmSideSwipeLeftEventListeners() throws BandIOException;

    void unregisterPalmSideSwipeLeftEventListener(PalmSideSwipeEventListener var1) throws BandIOException;

    boolean registerIntentionLockEventListener(IntentionLockEventListener listener) throws BandIOException;

    void unregisterIntentionLockEventListeners() throws BandIOException;

    void unregisterIntentionLockEventListener(IntentionLockEventListener listener) throws BandIOException;

}
