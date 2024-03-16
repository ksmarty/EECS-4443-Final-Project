package ca.yorku.eecs4443_finalproject_golf;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TestingActivity extends AppCompatActivity {

    String expectedText;
    String referenceText;

    ArrayList<TestResult> testResults;

    VIEWS currentView;

    /**
     * Timestamp when user started the task
     */
    long startTime;

    enum VIEWS {
        KEYBOARD_TEST,
        WELCOME
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentView(R.layout.activity_testing_welcome, VIEWS.WELCOME);

        testResults = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        String name = b.getString(BundleKeys.NAME);

        setupKeyboardListener();
    }

    private void setCurrentView(int contentView, VIEWS view) {
        currentView = view;
        setContentView(contentView);

        findViewById(R.id.testingContinueButton).setOnClickListener(this::continueButtonClicked);
    }

    /**
     * Modified from <a href="https://stackoverflow.com/a/26964010">Brownsoo Han on Stack Overflow</a>
     */
    private void setupKeyboardListener() {
        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            boolean isKeyboardShowing = keypadHeight > screenHeight * 0.15;

            onKeyboardVisibilityChanged(isKeyboardShowing);
        });

    }

    private void startTest() {
        startTime = System.currentTimeMillis();
    }

    private void continueButtonClicked(View view) {
        switch (currentView) {
            case WELCOME -> showTestScreen();
            case KEYBOARD_TEST -> recordTest();
        }
    }

    private void recordTest() {
        int completionTime = (int) (System.currentTimeMillis() - System.currentTimeMillis());

        String userText = ((EditText) findViewById(R.id.testingContentBox)).getText().toString();

        double errorRate = getErrorRate(userText);

        testResults.add(new TestResult(completionTime, errorRate));

        printResults();
    }

    private double getErrorRate(String userText) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        double userDistance = levenshteinDistance.apply(expectedText, userText);
        double referenceDistance = levenshteinDistance.apply(expectedText, referenceText);
        return userDistance / referenceDistance;
    }

    private void showTestScreen() {
        setCurrentView(R.layout.activity_testing_keyboard_test, VIEWS.KEYBOARD_TEST);

        EditText editText = findViewById(R.id.testingContentBox);

        // TODO add more tests & abstract
        referenceText = getString(R.string.test_content_1);
        expectedText = getString(R.string.test_expected_1);

        SpannableString spannableString = new SpannableString(referenceText);

        String highlightText = "potato";
        int startIndex = referenceText.indexOf(highlightText);
        int endIndex = startIndex + highlightText.length();

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        editText.setText(spannableString);

        startTest();
    }

    private void onKeyboardVisibilityChanged(boolean opened) {
        FloatingActionButton button = findViewById(R.id.testingContinueButton);
        button.setVisibility(opened ? View.GONE : View.VISIBLE);
    }

    private void printResults() {
        String prettyPrint = testResults.stream().map(TestResult::toString).collect(Collectors.joining());
        Log.i(null, prettyPrint);
    }
}