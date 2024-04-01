package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

class Helper {
    Context ctx;

    public Helper() {
        this.ctx = ApplicationProvider.getApplicationContext();
    }

    public Helper fillInput(int fieldId, String input) {
        onView(withId(fieldId))
                .perform(typeText(input));

        return this;
    }

    public Helper fillInput(int fieldId, int input) {
        return fillInput(fieldId, getString(input));
    }

    /**
     * Check if an input is invalid
     *
     * @param fieldId {@code R.id} of the field being checked
     * @param input   The string that will be entered in the field
     * @param error   {@code R.string} of the error message. -1 to check for no error
     */
    public Helper checkInvalidSetupInput(int fieldId, String input, int error) {
        fillInput(fieldId, input);

        clickButton(R.id.getStartedButton);

        onView(withId(fieldId))
                .check(matches(
                        error == -1
                                ? hasNoErrorText()
                                : hasErrorText(getString(error))
                ));

        return this;
    }

    /**
     * Check if an input is valid
     *
     * @param fieldId {@code R.id} of the field being checked
     * @param input   The string that will be entered in the field
     * @return
     */
    public Helper checkValidSetupInput(int fieldId, String input) {
        return checkInvalidSetupInput(fieldId, input, -1);
    }

    public Helper clickButton(int buttonId) {
        onView(withId(buttonId))
                .perform(click());

        return this;
    }

    public Helper clickContinue() {
        onView(ViewMatchers.isRoot()).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isRoot();
            }

            @Override
            public String getDescription() {
                return "Get the current view";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Do something with the current view
                Log.d("CurrentView", view.toString());
            }
        });

        return clickButton(R.id.testingContinueButton);
    }

    public Helper checkVisible(int view) {
        onView(withId(view))
                .check(matches(isDisplayed()));

        return this;
    }

    private String getString(int Id) {
        return ctx.getResources().getString(Id);
    }

    /**
     * Check if an EditText has no validation errors
     * <br/>
     * Via stamanuel on <a href="https://stackoverflow.com/a/42676338">stackoverflow</a>
     */
    public Matcher<? super View> hasNoErrorText() {
        return new BoundedMatcher<>(EditText.class) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("has no error text: ");
            }

            @Override
            protected boolean matchesSafely(EditText view) {
                return view.getError() == null;
            }
        };
    }
}
