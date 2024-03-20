package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import digitalink.DrawView;
import digitalink.StrokeManager;
import digitalink.StrokeManager.LANG;

import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_0;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_1;
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

        StrokeManager.init();

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
            case KEYBOARD_TEST, DIGITALINK_TEST -> testCompleted();
        }
    }

    private void testCompleted() {
        recordTest();
        showTestScreen();
    }

    private void recordTest() {
        // TODO implement attempts
        int attempts = 1;

        TestResult result = currentTest.complete(attempts);

        testResults.add(result);

        printResults();
    }

    private void showTestScreen() {
        // Pop next test from the list
        if(!allTests.isEmpty()){
            currentTest = allTests.remove(0);
            if (currentTest.type == KEYBOARD) setupKeyboard();
            else setupDigitalInk();
        }else {
            Log.i("DEBUG_ING", "There are no more tests");
            //We can collect results here and generate a qr code
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDigitalInk() {
        setCurrentView(R.layout.activity_testing_digitalink_test, VIEWS.DIGITALINK_TEST);

        StrokeManager.setModel(currentTest.language);

        Button clearButton = findViewById(R.id.clearButton);
        Button recognizeButton = findViewById(R.id.recognizeButton);
        DrawView drawView = findViewById(R.id.draw_view);
        TextView userInput = findViewById(R.id.userInput);
        TextView targetInput = findViewById(R.id.targetInput);
        TextView instructions = findViewById(R.id.testInstructions);

        targetInput.setText(currentTest.referenceText);
        instructions.setText(R.string.test_instructions_digital_ink);

        // Setup clear button
        clearButton.setOnClickListener(view -> {
            drawView.clear();
            StrokeManager.clear();
        });

        // Setup recognize button
        recognizeButton.setOnClickListener(view -> StrokeManager.recognize(userInput));
    }

    private void setupKeyboard() {
        setCurrentView(R.layout.activity_testing_keyboard_test, VIEWS.KEYBOARD_TEST);

        TextView targetInput = findViewById(R.id.targetInput);
        TextView instructions = findViewById(R.id.testInstructions);
        EditText userInput = findViewById(R.id.userInput);

        targetInput.setText(currentTest.referenceText);
        instructions.setText(R.string.test_instructions_keyboard);

        // Focus on the user input field
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Set keyboard language
        userInput.setImeHintLocales(new LocaleList(new Locale(getKeyboardLanguage())));

        imm.showSoftInput(userInput, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Mapping StrokeManager.LANG to <a href="https://en.wikipedia.org/wiki/IETF_language_tag#List_of_common_primary_language_subtags">BCP 47 codes</a>
     */
    private String getKeyboardLanguage() {
        return switch (currentTest.language) {
            case ENGLISH -> "en";
            case CHINESE -> "zh";
        };
    }

    private void onKeyboardVisibilityChanged(boolean opened) {
        FloatingActionButton button = findViewById(R.id.testingContinueButton);
        button.setVisibility(opened ? View.GONE : View.VISIBLE);
    }

    private void printResults() {
        String prettyPrint = testResults.stream().map(TestResult::toString).collect(Collectors.joining());
        Log.i(null, prettyPrint);
        Toast.makeText(this, prettyPrint, Toast.LENGTH_LONG).show();
    }

    private ArrayList<TestBundle> generateTests() {
        // TODO add more tests
        return new ArrayList<>(List.of(
                new TestBundle(this, KEYBOARD, test_content_0, LANG.ENGLISH),
                new TestBundle(this, DIGITALINK, test_content_0, LANG.ENGLISH),
                new TestBundle(this, KEYBOARD, test_content_1, LANG.CHINESE),
                new TestBundle(this, DIGITALINK, test_content_1, LANG.CHINESE)
        ));
    }

    enum VIEWS {
        KEYBOARD_TEST,
        DIGITALINK_TEST,
        WELCOME
    }
}