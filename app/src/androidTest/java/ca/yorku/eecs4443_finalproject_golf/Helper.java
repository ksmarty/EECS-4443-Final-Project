package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;
import android.graphics.Rect;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
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
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

class Helper {
    private static String TARGET_PACKAGE;
    Context ctx;

    UiDevice device;

    public Helper() {
        this.ctx = ApplicationProvider.getApplicationContext();
        TARGET_PACKAGE = ctx.getPackageName();
        device = UiDevice.getInstance(getInstrumentation());
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

    public Helper drawLine(double startX, double startY, double offsetX, double offsetY) {
        Rect bounds = device.findObject(By.res(TARGET_PACKAGE, "draw_view")).getVisibleBounds();

        final int MARGIN = 50;
        int width = bounds.width() - MARGIN * 2;
        int height = bounds.height() - MARGIN * 2;

        int normStartX = normalizeToRange(startX, width);
        int normStartY = normalizeToRange(startY, height);
        int normOffsetX = normalizeToRange(offsetX, width);
        int normOffsetY = normalizeToRange(offsetY, height);

        int baseX = MARGIN + bounds.left + normStartX;
        int baseY = bounds.centerY() - normStartY;

        device.drag(baseX, baseY, baseX + normOffsetX, baseY - normOffsetY, 10);

        return this;
    }

    private int normalizeToRange(double value, double max) {
        final int GRID_SIZE = 10;
        final int CHAR_SIZE = 2;

        double normalizedValue = value / CHAR_SIZE;
        return (int) (normalizedValue * (max / (GRID_SIZE / CHAR_SIZE)));
    }

    public void waitUntilLoaded() {
        UiObject2 getStartedButton = device.findObject(By.res(TARGET_PACKAGE, "getStartedButton"));
        getStartedButton.wait(Until.enabled(true), 2 * 60 * 1000);
    }

    public Helper waitForRecognizer() {
        UiObject2 userInput = device.findObject(By.res(TARGET_PACKAGE, "userInput"));
        userInput.wait(Until.textNotEquals(""), 3 * 1000);

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
