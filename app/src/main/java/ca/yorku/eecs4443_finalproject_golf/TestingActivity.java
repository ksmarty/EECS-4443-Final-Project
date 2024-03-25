package ca.yorku.eecs4443_finalproject_golf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import digitalink.DrawView;
import digitalink.StrokeManager;
import digitalink.StrokeManager.LANG;

import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_0;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_1;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_2;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_3;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_content_4;
import static ca.yorku.eecs4443_finalproject_golf.R.string.test_tutorial_complete;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.DIGITALINK;
import static ca.yorku.eecs4443_finalproject_golf.TestBundle.TYPE.KEYBOARD;
import static digitalink.StrokeManager.getLanguage;

public class TestingActivity extends AppCompatActivity {
    final int MAX_ATTEMPTS = 3;

    VIEWS currentView;

    TestBundle currentTest;

    Bundle bundle;
    TestSection currentSection;
    private int attempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentView(R.layout.activity_testing_welcome, VIEWS.WELCOME);

        currentSection = generateTutorials();

        bundle = getIntent().getExtras();

        // Write user data to CSV
        SharedPref.appendAndSaveCSVData(this, getUserData());
    }

    private void setCurrentView(int contentView, VIEWS view) {
        currentView = view;
        setContentView(contentView);

        findViewById(R.id.testingContinueButton).setOnClickListener(this::continueButtonClicked);
    }

    private void continueButtonClicked(View view) {
        switch (currentView) {
            case WELCOME, BREAK -> showTestScreen();
            case KEYBOARD_TEST, DIGITALINK_TEST -> testCompleted();
        }
    }

    private void testCompleted() {
        EditText input = findViewById(R.id.userInput);

        // Debounce when input is empty
        if (input.getText().toString().equals("")) return;

        if (Objects.requireNonNull(currentView) == VIEWS.KEYBOARD_TEST) {
            attempts++;
        }

        if (attempts <= MAX_ATTEMPTS && !isInputCorrect()) {
            Toast.makeText(this, getString(R.string.incorrect_answer), Toast.LENGTH_SHORT).show();
            clearInputs();
            return;
        }

        if (attempts > MAX_ATTEMPTS && Objects.requireNonNull(currentView) == VIEWS.KEYBOARD_TEST)
            recordTest();

        showBreakScreen();
    }


    private boolean isInputCorrect() {
        Locale locale = getCurrentLocale();

        String userInput = ((EditText) findViewById(R.id.userInput)).getText().toString();
        String referenceText = currentTest.referenceText;

        return referenceText.toLowerCase(locale).equals(userInput.toLowerCase(locale));
    }

    private void clearInputs() {
        EditText userInput = findViewById(R.id.userInput);

        if (Objects.requireNonNull(currentView) == VIEWS.DIGITALINK_TEST) {
            DrawView drawView = findViewById(R.id.draw_view);

            if (userInput == null || drawView == null) return;

            StrokeManager.clear();
            drawView.clear();
        }

        userInput.setText("");
    }


    private void recordTest() {
        if (Objects.requireNonNull(currentView) == VIEWS.KEYBOARD_TEST) attempts++;

        currentSection.completeTest(currentTest, attempts);

        logResults();
    }

    private void showBreakScreen() {
        setCurrentView(R.layout.activity_testing_break, VIEWS.BREAK);

        int totalTests = currentSection.size;
        int remainingTests = totalTests - currentSection.tests.size();

        TextView breakTitle = findViewById(R.id.breakTitle);
        TextView breakText = findViewById(R.id.breakText);

        String content = String.format(getString(R.string.test_break), currentSection.name, remainingTests, totalTests);
        breakTitle.setText(content);

        if (!TextUtils.isEmpty(currentTest.breakText))
            breakText.setText(currentTest.breakText);

        if (!currentSection.tests.isEmpty()) return;

        // Save data
        currentSection.writeResults(this);

        if (currentSection.isTest()) return;

        // Switch from tutorial to tests
        currentSection = generateTests();
    }

    private void showTestScreen() {
        if (currentSection.tests.isEmpty() && currentSection.isTest()) {
            // Show results screen
            allTestsCompleted();
            return;
        }

        // Pop next test from the list
        currentTest = currentSection.getNextTest();
        currentTest.start();

        // Reset attempts
        attempts = 0;

        if (currentTest.type == KEYBOARD) setupKeyboard();
        else setupDigitalInk();
    }

    private void allTestsCompleted() {
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
        hideKeyboard(); // Fix if keyboard still on screen.

        setCurrentView(R.layout.activity_testing_digitalink_test, VIEWS.DIGITALINK_TEST);

        StrokeManager.setModel(currentTest.language);

        Button clearButton = findViewById(R.id.clearButton);
        Button recognizeButton = findViewById(R.id.recognizeButton);
        EditText userInput = findViewById(R.id.userInput);
        TextView targetInput = findViewById(R.id.targetInput);
        TextView instructions = findViewById(R.id.testInstructions);

        targetInput.setText(currentTest.referenceText);
        instructions.setText(R.string.test_instructions_digital_ink);

        StrokeManager.clear();

        // Setup clear button
        clearButton.setOnClickListener(view -> clearInputs());

        // Setup recognize button
        recognizeButton.setOnClickListener(view -> {
            if (!StrokeManager.hasStrokes()) return;

            attempts++;
            StrokeManager.recognize(userInput, currentTest.language);
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
        userInput.setImeHintLocales(new LocaleList(getCurrentLocale()));

        imm.showSoftInput(userInput, InputMethodManager.SHOW_IMPLICIT);

        userInput.addTextChangedListener(getTextWatcher());
    }

    private Locale getCurrentLocale() {
        return new Locale(getLanguage(currentTest.language));
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
        String prettyPrint = currentSection.getPrettyPrint();
        Log.i(null, prettyPrint);
    }

    private TestSection generateTutorials() {
        ArrayList<TestBundle> tutorials = new ArrayList<>(List.of(
                new TestBundle(this, KEYBOARD, test_content_0, LANG.ENGLISH),
                new TestBundle(this, DIGITALINK, test_content_0, LANG.ENGLISH, test_tutorial_complete)
        ));

        return new TestSection(this, tutorials, R.string.tutorial, TestSection.TYPE.TUTORIAL);
    }

    private TestSection generateTests() {
        ArrayList<TestBundle> tests = new ArrayList<>(List.of(
                new TestBundle(this, KEYBOARD, test_content_1, LANG.GEORGIAN),
                new TestBundle(this, DIGITALINK, test_content_1, LANG.GEORGIAN),
                new TestBundle(this, KEYBOARD, test_content_2, LANG.GREEK),
                new TestBundle(this, DIGITALINK, test_content_2, LANG.GREEK),
                new TestBundle(this, KEYBOARD, test_content_3, LANG.ARMENIAN),
                new TestBundle(this, DIGITALINK, test_content_3, LANG.ARMENIAN),
                new TestBundle(this, KEYBOARD, test_content_4, LANG.UKRAINIAN),
                new TestBundle(this, DIGITALINK, test_content_4, LANG.UKRAINIAN)
        ));

        return new TestSection(this, tests, R.string.test, TestSection.TYPE.TEST);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    enum VIEWS {
        KEYBOARD_TEST,
        DIGITALINK_TEST,
        WELCOME,
        BREAK
    }
}