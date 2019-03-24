package com.zvecr.jmeter.util;

import org.apache.jmeter.testelement.TestStateListener;

/**
 * Java 8 style {@link TestStateListener} which defaults all methods for cleaner implementation
 */
public interface DefaultedTestStateListener extends TestStateListener {

    @Override
    default void testStarted() {
    }

    @Override
    default void testStarted(String host) {
    }

    @Override
    default void testEnded() {
    }

    @Override
    default void testEnded(String host) {
    }
}
