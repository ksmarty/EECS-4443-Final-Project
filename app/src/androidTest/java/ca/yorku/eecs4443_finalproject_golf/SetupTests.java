package ca.yorku.eecs4443_finalproject_golf;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class SetupTests {
    @Rule
    public ActivityScenarioRule<SetupActivity> activityRule = new ActivityScenarioRule<>(SetupActivity.class);
    Helper helper;

    @BeforeClass
    public static void beforeClass() {
        // Prevent timeout while loading languages
        IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(2, TimeUnit.MINUTES);
    }

    @Before
    public void reset() {
        onView(withId(R.id.nameField)).perform(clearText());
        onView(withId(R.id.ageField)).perform(clearText());

        helper = new Helper();
    }

    @Test
    public void testInvalidName() {
        helper.checkInvalidSetupInput(R.id.nameField, "AB", R.string.error_invalid_name);
    }

    @Test
    public void testValidName() {
        helper.checkValidSetupInput(R.id.nameField, "Steve");
    }

    @Test
    public void testInvalidAgeUnder() {
        helper.checkInvalidSetupInput(R.id.ageField, "17", R.string.error_invalid_age);
    }

    @Test
    public void testInvalidAgeOver() {
        helper.checkInvalidSetupInput(R.id.ageField, "121", R.string.error_invalid_age);
    }

    @Test
    public void testValidAge() {
        helper.checkValidSetupInput(R.id.ageField, "25");
    }

    @Test
    public void testValidSetup() {
        helper
                .fillInput(R.id.nameField, "Steve")
                .fillInput(R.id.ageField, "25")
                .clickButton(R.id.getStartedButton)
                .checkVisible(R.id.testingWelcomeLayout);
    }

    @Test
    public void testEnglishKeyboard() {
        testValidSetup();

        helper
                .clickContinue()
                .fillInput(R.id.userInput, R.string.test_content_0)
                .clickContinue()
                .checkVisible(R.id.testingBreakLayout);
    }
}