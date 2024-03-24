package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE;
import static digitalink.StrokeManager.LANG;

public class TestResult {
    /**
     * Time to complete in ms
     */
    long time;

    /**
     * Number of attempts made
     */
    int attempts;

    /**
     * Type of test - Keyboard or Digital Ink
     */
    TYPE type;

    /**
     * Test language
     */
    LANG language;

    /**
     * CPU time in ms
     */
    long cpuTime;

    public TestResult(long time, int attempts, TYPE type, LANG language, long cpuTime) {
        this.time = time;
        this.attempts = attempts;
        this.type = type;
        this.language = language;
        this.cpuTime = cpuTime;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("---%nTest Result:%nTime: %d ms%nAttempts: %d%nType: %s%nLanguage: %s%n", time, attempts, type.toString(), language.toString());
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%s,%s%n", time, attempts, type, language, cpuTime);
    }
}
