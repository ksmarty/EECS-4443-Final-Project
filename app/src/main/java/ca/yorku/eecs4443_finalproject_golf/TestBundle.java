package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.UUID;

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
     * The text that will result in a 0% error rate
     */
    String expectedText;

    /**
     * The text that the user needs to modify or interact with
     */
    String highlightText;

    /**
     * Timestamp when user started the task
     */
    long startTime;

    public TestBundle(Context ctx, TYPE type, int referenceText, int expectedText, int highlightText) {
        this.type = type;
        this.referenceText = ctx.getString(referenceText);
        this.expectedText = ctx.getString(expectedText);
        this.highlightText = ctx.getString(highlightText);
        startTime = System.currentTimeMillis();
        ID = UUID.randomUUID();
    }

    public SpannableString getContent() {
        SpannableString spannableString = new SpannableString(referenceText);

        int startIndex = referenceText.indexOf(highlightText);
        int endIndex = startIndex + highlightText.length();

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public TestResult complete(String userText) {
        int completionTime = (int) (System.currentTimeMillis() - startTime);

        double errorRate = getErrorRate(userText);

        return new TestResult(completionTime, errorRate);
    }

    private double getErrorRate(String userText) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        double userDistance = levenshteinDistance.apply(expectedText, userText);
        double referenceDistance = levenshteinDistance.apply(expectedText, referenceText);
        return userDistance / referenceDistance;
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
