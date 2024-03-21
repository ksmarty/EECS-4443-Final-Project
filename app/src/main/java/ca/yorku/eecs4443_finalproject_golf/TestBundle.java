package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;
import android.os.Debug;

import java.util.UUID;

import digitalink.StrokeManager.LANG;

public class TestBundle implements Comparable<TestBundle> {
    /**
     * ID of the bundle
     */
    private final UUID ID;

    /**
     * The test type
     */
    TYPE type;

    /**
     * The starting text that will be shown to the user
     */
    String referenceText;

    /**
     * Timestamp when user started the task
     */
    long startTime;

    /**
     * CPU thread time at test start
     */
    long cpuStart;

    LANG language;

    public TestBundle(Context ctx, TYPE type, int referenceText, LANG language) {
        this.type = type;
        this.referenceText = ctx.getString(referenceText);
        this.language = language;
        ID = UUID.randomUUID();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        cpuStart = Debug.threadCpuTimeNanos();
    }

    public TestResult complete(int attempts) {
        long completionTime = (System.currentTimeMillis() - startTime);
        long cpuTime = (Debug.threadCpuTimeNanos() - cpuStart) / 1_000_000;

        return new TestResult(completionTime, attempts, type, language, cpuTime);
    }

    @Override
    public int compareTo(TestBundle other) {
        return this.ID.compareTo(other.ID);
    }

    public enum TYPE {
        KEYBOARD,
        DIGITALINK
    }
}
