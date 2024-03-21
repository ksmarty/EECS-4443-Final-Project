package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE;
import static digitalink.StrokeManager.LANG;

public class TestResult {
    /**
     * Time to complete in ms
     */
    int time;

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

    public TestResult(int time, int attempts, TYPE type, LANG language) {
        this.time = time;
        this.attempts = attempts;
        this.type = type;
        this.language = language;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("---%nTest Result:%nTime: %d ms%nAttempts: %d%nType: %s%nLanguage: %s", time, attempts, type.toString(), language.toString());
    }

    public String toCSV(String name) {
        return String.format("%s,%s,%s,%s,%s%n", name, time, attempts, type, language);
    }
}
