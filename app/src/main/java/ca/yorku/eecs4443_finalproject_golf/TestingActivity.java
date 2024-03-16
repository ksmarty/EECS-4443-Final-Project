package ca.yorku.eecs4443_finalproject_golf;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_1;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_expected_1;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_highlight_1;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.DIGITALINK;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.KEYBOARD;

public class TestingActivity extends AppCompatActivity {
    ArrayList<TestResult> testResults;

    VIEWS currentView;

    ArrayList<TestBundle> allTests;

    TestBundle currentTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentView(R.layout.activity_testing_welcome, VIEWS.WELCOME);

        testResults = new ArrayList<>();
        allTests = generateTests();

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

    private void continueButtonClicked(View view) {
        switch (currentView) {
            case WELCOME -> showTestScreen();
            case KEYBOARD_TEST -> recordTest();
        }
    }

    private void recordTest() {
        String userText = ((EditText) findViewById(R.id.testingContentBox)).getText().toString();

        TestResult result = currentTest.complete(userText);

        testResults.add(result);

        printResults();
    }

    private void showTestScreen() {
        setCurrentView(R.layout.activity_testing_keyboard_test, VIEWS.KEYBOARD_TEST);

        EditText editText = findViewById(R.id.testingContentBox);

        // Pop next test from the list
        currentTest = allTests.remove(0);

        editText.setText(currentTest.getContent());
    }

    private void onKeyboardVisibilityChanged(boolean opened) {
        FloatingActionButton button = findViewById(R.id.testingContinueButton);
        button.setVisibility(opened ? View.GONE : View.VISIBLE);
    }

    private void printResults() {
        String prettyPrint = testResults.stream().map(TestResult::toString).collect(Collectors.joining());
        Log.i(null, prettyPrint);
    }

    private ArrayList<TestBundle> generateTests() {
        // TODO add more tests
        return new ArrayList<>(List.of(
                new TestBundle(this, KEYBOARD, test_content_1, test_expected_1, test_highlight_1),
                new TestBundle(this, DIGITALINK, test_content_1, test_expected_1, test_highlight_1)
        ));
    }

    enum VIEWS {
        KEYBOARD_TEST,
        WELCOME
    }
}