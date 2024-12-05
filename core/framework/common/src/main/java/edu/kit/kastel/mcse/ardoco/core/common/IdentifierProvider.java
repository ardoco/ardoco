/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.concurrent.atomic.AtomicInteger;

public class IdentifierProvider {
    private static final int INITIAL_VALUE = 1337;
    private static final AtomicInteger COUNTER = new AtomicInteger(INITIAL_VALUE);

    private IdentifierProvider() {
        // empty
    }

    public static String createId() {
        int currentCounter = COUNTER.getAndIncrement();
        return "acm%09djsd".formatted(currentCounter);
    }

    public static void reset() {
        COUNTER.set(INITIAL_VALUE);
    }
}
