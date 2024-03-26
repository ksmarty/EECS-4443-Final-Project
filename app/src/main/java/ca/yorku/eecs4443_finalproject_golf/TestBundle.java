package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;
import android.os.Debug;

import java.util.UUID;

import digitalink.StrokeManager.LANG;

public class TestBundle implements Comparable<TestBundle> {
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

    /**
     * Text to show on the break screen after the test is completed
     */
    String breakText;

    /**
     * ID of the bundle
     */
    private UUID ID;

    public TestBundle(Context ctx, TYPE type, int referenceText, LANG language) {
        initialize(ctx, type, referenceText, language);
    }

    public TestBundle(TestingActivity ctx, TYPE type, int referenceText, LANG language, int breakText) {
        this.breakText = ctx.getString(breakText);
        initialize(ctx, type, referenceText, language);
    }

    private void initialize(Context ctx, TYPE type, int referenceText, LANG language) {
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
        long completionTime = (System.currentTimeMillis() - startTime)/1000;
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
