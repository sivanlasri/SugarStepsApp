package com.example.sugarsteps;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.hamcrest.core.IsNot.not;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.sugarsteps.lesson.LessonsListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LessonListTest {

    @Rule
    public ActivityScenarioRule<LessonsListActivity> activityRule =
            new ActivityScenarioRule<>(LessonsListActivity.class);

    @Test
    public void openLessonDetailsAndCheckViews() throws InterruptedException {
        // Wait for the list to loa
        Thread.sleep(1000);

        // Verify RecyclerView exists and has items
        onView(withId(R.id.recyclerview))
                .check(matches(isDisplayed()))
                .check(matches(hasMinimumChildCount(1)));

        // Click on first lesson in the RecyclerView
        onView(withId(R.id.recyclerview))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait longer for the lesson details to load completely
        Thread.sleep(3000);

        // Check that we're not in the list view anymore
        try {
            onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
            throw new AssertionError("Still in list view - navigation failed!");
        } catch (androidx.test.espresso.NoMatchingViewException e) {
            // Good - we're not in the list view anymore
        }

        // Check that all main views are displayed
        onView(withId(R.id.tv_lesson_header)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_lesson_guide)).check(matches(isDisplayed()));
        onView(withId(R.id.video_lesson)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_lesson_recipe)).check(matches(isDisplayed()));
        onView(withId(R.id.imgbtn_like_lesson)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_lesson_check)).check(matches(isDisplayed()));

        // Check if text is displayed
        onView(withId(R.id.tv_lesson_header))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))));

        // Check if Instructor Name is displayed and not empty
        onView(withId(R.id.tv_lesson_guide))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))));

        // Check if VideoView is displayed
        onView(withId(R.id.video_lesson))
                .check(matches(isDisplayed()));

        // Check if Description is displayed and not empty
        onView(withId(R.id.tv_lesson_recipe))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))));

        // Check if "Like" button is displayed and clickable
        onView(withId(R.id.imgbtn_like_lesson))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        // Check if Checkbox is displayed and clickable
        onView(withId(R.id.btn_lesson_check))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        // Test functionality: click on the "Like" button
        onView(withId(R.id.imgbtn_like_lesson))
                .perform(click());

        // Test functionality: toggle the checkbox
        onView(withId(R.id.btn_lesson_check))
                .perform(click());

        // Wait a bit for the checkbox state to change
        Thread.sleep(500);

        // Check that checkbox is now checked
        onView(withId(R.id.btn_lesson_check))
                .check(matches(isChecked()));

        // Toggle checkbox again to test unchecking
        onView(withId(R.id.btn_lesson_check))
                .perform(click());

        Thread.sleep(500);

        // Check that checkbox is now unchecked
        onView(withId(R.id.btn_lesson_check))
                .check(matches(not(isChecked())));

        // Stop video
        try {
            onView(withId(R.id.video_lesson))
                    .check(matches(isDisplayed()));
            // Give time to video to shut down
            Thread.sleep(1000);
        } catch (Exception e) {
            // No Video, That's OK
        }

        // Checking that the back button is working
        onView(withId(R.id.btn_back))
                .check(matches(isClickable()))
                .perform(click());

        // Waiting for it to load
        Thread.sleep(1000);

        // Check that we went back to the LessonList page
        onView(withId(R.id.recyclerview))
                .check(matches(isDisplayed()));
    }

}