package com.example.sugarsteps.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sugarsteps.lesson.LessonsListActivity;
import com.example.sugarsteps.R;

/**
 * Main entry point activity for the SugarSteps application.
 *
 * This activity serves as a splash screen that displays the app logo/branding
 * for a brief period while determining the user's registration status.
 * Based on the user's registration state, it automatically navigates to either
 * the registration flow or the main lessons list.
 *
 * Features:
 * - Splash screen functionality with branded UI
 * - Automatic user registration status detection
 * - Smart navigation routing based on user state
 * - Smooth transition to appropriate activity
 * - SharedPreferences integration for persistent user data
 *
 * Navigation Flow:
 * 1. Display splash screen for 1.5 seconds
 * 2. Check SharedPreferences for registration status
 * 3. Route to RegistrationActivity (if not registered) or LessonsListActivity (if registered)
 * 4. Finish current activity to prevent back navigation to splash
 *
 * @author Sivan Lasri
 * @version 6.0
 *
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     *
     * Initializes the splash screen UI and sets up a delayed navigation system.
     * The method displays the splash screen layout and uses a Handler to create
     * a timed delay before checking user registration status and navigating
     * to the appropriate next activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     *                          previously shut down, this Bundle contains the data
     *                          it most recently supplied. Otherwise, it is null.
     *                          Not used in this implementation as no state needs restoration.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for 2 seconds then navigate
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            /**
             * Runnable executed after the splash delay period.
             *
             * This method is called after 1.5 seconds and performs the core routing logic:
             * 1. Checks user registration status from SharedPreferences
             * 2. Creates appropriate Intent based on registration state
             * 3. Starts the target activity
             * 4. Finishes current activity to prevent back navigation to splash
             *
             * Navigation Rules:
             * - If user is registered (isRegistered = true): Navigate to LessonsListActivity
             * - If user is not registered (isRegistered = false): Navigate to RegistrationActivity
             */
            @Override
            public void run() {
                // Check if user is registered
                SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
                boolean isRegistered = prefs.getBoolean("isRegistered", false);

                // Navigate to appropriate activity
                Intent intent;
                if (isRegistered) {
                    intent = new Intent(MainActivity.this, LessonsListActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, RegistrationActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, 1500); // 1.5 seconds delay
    }
}