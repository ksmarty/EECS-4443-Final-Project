package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class TestResult {
    /**
     * Time to complete in ms
     */
    int time;

    /**
     * levenshteinDistance(expected, user) / levenshteinDistance(expected, original)
     */
    double errorRate;

    TestBundle.TYPE type;

    public TestResult(int time, double errorRate, TestBundle.TYPE type) {
        this.time = time;
        this.errorRate = errorRate;
        this.type = type;
    }

    public int getTime() {
        return time;
    }

    public double getErrorRate() {
        return errorRate;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("---%nTest Result:%nTime: %d ms%nError Rate: %.2f%%%nType: %s", time, errorRate * 100, type.toString());
    }
}
