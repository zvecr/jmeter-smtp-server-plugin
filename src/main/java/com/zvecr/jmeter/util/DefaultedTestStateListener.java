package com.zvecr.jmeter.util;

import org.apache.jmeter.testelement.TestStateListener;

/**
 * Java 8 style {@link TestStateListener} which defaults all methods for cleaner implementation
 */
public interface DefaultedTestStateListener extends TestStateListener {

    @Override
    default void testStarted() {
        // Do nothing 
    }

    @Override
    default void testStarted(String host) {
        // Do nothing
    }

    @Override
    default void testEnded() {
        // Do nothing
    }

    @Override
    default void testEnded(String host) {
        // Do nothing
    }
}
