package com.example.sugarsteps;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.sugarsteps.user.RegistrationActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegistrationActivityTest {

    @Rule
    public ActivityScenarioRule<RegistrationActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(RegistrationActivity.class); // Move to Registration Activity

    @Test
    public void RegistrationActivityTest() throws InterruptedException {


        // Type a username and submit it
        onView(withId(R.id.et_username_text))
                .perform(replaceText("סיון"), closeSoftKeyboard(), pressImeActionButton());

        // Click on "Guide" button
        onView(withId(R.id.btn_registration_guide)).perform(click());

        // Check the declaration checkbox
        onView(withId(R.id.chk_declaration)).perform(click());

        // Click "Cancel" to trigger warning
        onView(withId(R.id.btn_cancel)).perform(click());

        // Verify the warning text appears
        onView(withId(R.id.tv_warning_cant_continue))
                .check(matches(withText("חייב לקבל את התנאים כדי להמשיך.")))
                .check(matches(isDisplayed()));

        // Re-check the declaration checkbox
        onView(withId(R.id.chk_declaration)).perform(click());

        // Click "Confirm"
        onView(withId(R.id.btn_confirm)).perform(click());

        Thread.sleep(1000); // Wait for transition

        // Click "Let's go!"
        onView(withId(R.id.btn_lets_go)).perform(click());

        // Now verify we are in the next activity correctly by checking an add_fab is visible - Because User is Guide
        onView(withId(R.id.fab_add))
                .check(matches(isDisplayed()));
    }
}
