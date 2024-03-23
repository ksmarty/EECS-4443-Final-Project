package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
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
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.DIGITALINK;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.KEYBOARD;

public class TestingActivity extends AppCompatActivity {
    final int MAX_ATTEMPTS = 3;

    ArrayList<TestResult> testResults;

    VIEWS currentView;

    ArrayList<TestBundle> allTests;

    TestBundle currentTest;

    Bundle bundle;

    private int attempts = 1;


    NonDeletableEditTextWatcher nonDeletableEditTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentView(R.layout.activity_testing_welcome, VIEWS.WELCOME);

        testResults = new ArrayList<>();
        allTests = generateTests();

        bundle = getIntent().getExtras();

        StrokeManager.init();

        // setupKeyboardListener();
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
        if(attempts < MAX_ATTEMPTS && !checkCorrectness()){
            attempts++;
            clearInputs();
            return;
        }
        recordTest();
        showTestScreen();
    }


    private boolean checkCorrectness() {
        switch (currentView) {
            case KEYBOARD_TEST -> {
                TextView targetInput = findViewById(R.id.targetInput);
                EditText userInput = findViewById(R.id.userInput);
                return targetInput.getText().toString().equals(userInput.getText().toString());
            }
            case DIGITALINK_TEST -> {
                TextView targetInput = findViewById(R.id.targetInput);
                TextView input = findViewById(R.id.userInput);
                return targetInput.getText().toString().equals(input.getText().toString());
            }
        }
        return false;
    }

    private void clearInputs() {
        switch (currentView) {
            case DIGITALINK_TEST:
                TextView digitalInkUserInput = findViewById(R.id.userInput);
                DrawView drawView = findViewById(R.id.draw_view);
                if (digitalInkUserInput != null && drawView != null) {
                    digitalInkUserInput.setText("");
                    StrokeManager.clear();
                    drawView.clear();
                }
                break;
            case KEYBOARD_TEST:
                if(nonDeletableEditTextWatcher == null) { //From current contentView
                    EditText inputUser = (EditText) findViewById(R.id.userInput);
                    nonDeletableEditTextWatcher = new NonDeletableEditTextWatcher(inputUser);
                }
                nonDeletableEditTextWatcher.clear(); //Clear input data on every attempt
                break;
        }
    }


    private void recordTest() {

        TestResult result = currentTest.complete(attempts);

        attempts = 1;

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
        hideKeyboard(this); //Fix if keyboard still on screen.

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

        clearButton.setVisibility(View.INVISIBLE);

        StrokeManager.clear();

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
        nonDeletableEditTextWatcher = new NonDeletableEditTextWatcher(userInput);//Setup from a new contentView
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

    private void logResults() {
        String prettyPrint = testResults.stream().map(TestResult::toString).collect(Collectors.joining());
        Log.i(null, prettyPrint);
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