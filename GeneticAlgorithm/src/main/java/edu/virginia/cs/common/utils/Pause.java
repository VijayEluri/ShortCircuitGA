/*
 * Copyright (c) 2010-2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.common.utils;

import java.io.File;

import javax.swing.JFrame;

/**
 * Utility class for various pause methods
 * @author <a href="mailto:benjaminhocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Jul 17, 2010
 */
public class Pause {

    private static final int SLEEP_INTERVAL = 50; // milliseconds

    /**
     * Pauses until a condition is met, or a certain time has elapsed
     * @param c Condition that needs to be met (a null Condition will never be met)
     * @param maxWait Maximum number of milliseconds to wait for
     * @return Whether the condition was met
     */
    public static boolean untilConditionMet(final Condition c, final int maxWait) {
        boolean retval = c != null && c.met();
        int timeElapsed = 0;
        while ((!retval) && timeElapsed < maxWait) {
            try {
                Thread.sleep(SLEEP_INTERVAL);
            }
            catch (final InterruptedException e) {
                break; /* If interrupted, then stop looking for the condition to be met */
            }
            retval = c != null && c.met();
            timeElapsed += SLEEP_INTERVAL;
        }
        return retval;
    }

    /**
     * Pauses until a file exists or maxWait milliseconds has elapsed.
     * @param f File whose existence is being waited for
     * @param maxWait Maximum number of milliseconds to wait for
     * @return Whether the file exists
     */
    public static boolean untilExists(final File f, final int maxWait) {
        final Condition c = new Condition() {

            @Override
            public boolean met() {
                return f.exists();
            }
        };
        return untilConditionMet(c, maxWait);
    }

    /**
     * Pauses until a frame no longer is visible or maxWait milliseconds has elapsed
     * @param frame Frame to wait while it's still visible
     * @param maxWait Maximum number of milliseconds to wait for
     * @return Whether the frame is still visible
     */
    public static boolean whileVisible(final JFrame frame, final int maxWait) {
        final Condition c = new Condition() {

            @Override
            public boolean met() {
                return !frame.isVisible();
            }
        };
        return !untilConditionMet(c, maxWait);
    }
}
