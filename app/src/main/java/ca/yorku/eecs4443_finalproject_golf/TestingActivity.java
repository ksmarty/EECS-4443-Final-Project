package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import digitalink.DrawView;
import digitalink.StrokeManager;
import digitalink.StrokeManager.LANG;

import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_0;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_1;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_2;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_3;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_4;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.DIGITALINK;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.KEYBOARD;
import static digitalink.StrokeManager.getLanguage;

public class TestingActivity extends AppCompatActivity {
    final int MAX_ATTEMPTS = 3;

    ArrayList<TestResult> testResults;

    VIEWS currentView;

    ArrayList<TestBundle> allTests;

    TestBundle currentTest;

    Bundle bundle;
    private int attempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentView(R.layout.activity_testing_welcome, VIEWS.WELCOME);

        testResults = new ArrayList<>();
        allTests = generateTests();

        bundle = getIntent().getExtras();

        StrokeManager.init();
    }

    private void setCurrentView(int contentView, VIEWS view) {
        currentView = view;
        setContentView(contentView);

        findViewById(R.id.testingContinueButton).setOnClickListener(this::continueButtonClicked);
    }

    private void continueButtonClicked(View view) {
        switch (currentView) {
            case WELCOME -> showTestScreen();
            case KEYBOARD_TEST, DIGITALINK_TEST -> testCompleted();
        }
    }

    private void testCompleted() {
        switch (currentView) {
            case KEYBOARD_TEST -> attempts++;
            case DIGITALINK_TEST -> {
                TextView input = findViewById(R.id.userInput);
                // If user forgot to press "recognize", just ignore button press
                if (input.getText().toString().equals("") && StrokeManager.hasStrokes()) return;
            }
        }

        if (attempts <= MAX_ATTEMPTS && !isInputCorrect()) {
            clearInputs();
            return;
        }

        showTestScreen();
    }


    private boolean isInputCorrect() {
        TextView targetInput = findViewById(R.id.targetInput);

        switch (currentView) {
            case KEYBOARD_TEST -> {
                EditText userInput = findViewById(R.id.userInput);
                return targetInput.getText().toString().equals(userInput.getText().toString());
            }
            case DIGITALINK_TEST -> {
                TextView input = findViewById(R.id.userInput);
                return targetInput.getText().toString().equals(input.getText().toString());
            }
        }
        return false;
    }

    private void clearInputs() {
        switch (currentView) {
            case DIGITALINK_TEST -> {
                TextView digitalInkUserInput = findViewById(R.id.userInput);
                DrawView drawView = findViewById(R.id.draw_view);

                if (digitalInkUserInput == null || drawView == null) return;

                digitalInkUserInput.setText("");
                StrokeManager.clear();
                drawView.clear();
            }
            case KEYBOARD_TEST -> {
                EditText inputUser = findViewById(R.id.userInput);
                inputUser.setText("");
            }
        }
    }


    private void recordTest() {
        TestResult result = currentTest.complete(Math.max(1, attempts));

        testResults.add(result);

        logResults();
    }

    private void showTestScreen() {
        // Show results screen
        if (allTests.isEmpty()) {
            allTestsCompleted();
            return;
        }

        // Pop next test from the list
        currentTest = allTests.remove(0);
        currentTest.start();

        // Reset attempts
        attempts = 0;

        if (currentTest.type == KEYBOARD) setupKeyboard();
        else setupDigitalInk();
    }

    private void allTestsCompleted() {
        // User data
        String userData = getUserData();

        // Create CSV data
        String csv = userData.concat(testResults.stream().map(TestResult::toCSV).collect(Collectors.joining()));

        // Save data
        SharedPref.appendAndSaveCSVData(this, csv);

        // Open Result Activity
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
        this.finish(); // Close current activity.
    }

    @NonNull
    private String getUserData() {
        String name = bundle.getString(BundleKeys.NAME, "UNKNOWN_NAME");
        int age = bundle.getInt(BundleKeys.AGE, 0);
        String gender = bundle.getString(BundleKeys.GENDER, "UNKNOWN_GENDER");
        String experience = bundle.getString(BundleKeys.EXPERIENCE, "UNKNOWN_EXPERIENCE");
        return String.format("%s,%s,%s,%s%n", name, age, gender, experience);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDigitalInk() {
        hideKeyboard(this); // Fix if keyboard still on screen.

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

        StrokeManager.clear();

        // Setup clear button
        clearButton.setOnClickListener(view -> {
            drawView.clear();
            userInput.setText("");
            StrokeManager.clear();
        });

        // Setup recognize button
        recognizeButton.setOnClickListener(view -> {
            attempts++;
            StrokeManager.recognize(userInput);
        });

        userInput.addTextChangedListener(getTextWatcher());
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
        userInput.setImeHintLocales(new LocaleList(new Locale(getLanguage(currentTest.language))));

        imm.showSoftInput(userInput, InputMethodManager.SHOW_IMPLICIT);

        userInput.addTextChangedListener(getTextWatcher());
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInputCorrect()) {
                    recordTest();
                }
            }
        };
    }

    private void logResults() {
        String prettyPrint = testResults.stream().map(TestResult::toString).collect(Collectors.joining());
        Log.i(null, prettyPrint);
    }

    private ArrayList<TestBundle> generateTests() {
        // TODO add more tests
        return new ArrayList<>(List.of(
                new TestBundle(this, KEYBOARD, test_content_0, LANG.ENGLISH),
                new TestBundle(this, DIGITALINK, test_content_0, LANG.ENGLISH),
                new TestBundle(this, KEYBOARD, test_content_1, LANG.GEORGIAN),
                new TestBundle(this, DIGITALINK, test_content_1, LANG.GEORGIAN),
                new TestBundle(this, KEYBOARD, test_content_2, LANG.GREEK),
                new TestBundle(this, DIGITALINK, test_content_2, LANG.GREEK),
                new TestBundle(this, KEYBOARD, test_content_3, LANG.ARMENIAN),
                new TestBundle(this, DIGITALINK, test_content_3, LANG.ARMENIAN),
                new TestBundle(this, KEYBOARD, test_content_4, LANG.UKRAINIAN),
                new TestBundle(this, DIGITALINK, test_content_4, LANG.UKRAINIAN)
        ));
    }

    private void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    enum VIEWS {
        KEYBOARD_TEST,
        DIGITALINK_TEST,
        WELCOME
    }
}