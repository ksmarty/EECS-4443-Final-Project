package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;

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

    LANG language;

    public TestBundle(Context ctx, TYPE type, int referenceText, LANG language) {
        this.type = type;
        this.referenceText = ctx.getString(referenceText);
        this.language = language;
        startTime = System.currentTimeMillis();
        ID = UUID.randomUUID();
    }

    public TestResult complete(int attempts) {
        int completionTime = (int) (System.currentTimeMillis() - startTime);

        return new TestResult(completionTime, attempts, type, language);
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
