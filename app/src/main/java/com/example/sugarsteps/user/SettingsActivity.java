package com.example.sugarsteps.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sugarsteps.R;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private User currentUser;                // The currently logged-in user object
    private UserViewModel userViewModel;    // ViewModel to access user data from the database
    private MaterialButton studentBtn;      // Button to select "student" role
    private MaterialButton guideBtn;        // Button to select "guide" role
    private MaterialButton saveBtn;         // Button to save changes
    private MaterialButton cancelBtn;       // Button to cancel and exit without saving
    private MaterialButton btnBackground1,btnBackground2; // Buttons for choosing background
    private int selectedBackground = 1; // Number of Background - default
    private Spinner levelSpn;               // Spinner to select user level (e.g., beginner, advanced)
    private Spinner genderSpn;              // Spinner to select user gender
    private EditText phoneEdt;              // Input field for user's phone number
    private EditText ageEdt;                // Input field for user's age
    private ImageButton backBtn;            // Back button to cancel and exit


    private static final String KEY_BACKGROUND = "selected_background"; // For SharedPreferences Background of app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loading background from preferences
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
        selectedBackground = prefs.getInt(KEY_BACKGROUND, 1);

        setContentView(R.layout.activity_settings);

        // Initialize UI components by finding their IDs
        initViews();

        // Setup listeners for buttons and input validations
        setupRoleButtons();
        setupListeners();
        updateBackgroundButtonsUI();

        // Set up Background according to choice
        setAppBackground(selectedBackground);


        // Initialize ViewModel to interact with user data
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Load user data from DB and observe for changes
        observeUserData();
    }

    /**
     * Find all UI views by their IDs.
     */
    private void initViews() {
        studentBtn = findViewById(R.id.btn_student_settings);
        guideBtn = findViewById(R.id.btn_guide_settings);
        saveBtn = findViewById(R.id.btn_save_settings);
        cancelBtn = findViewById(R.id.btn_cancel_settings);
        levelSpn = findViewById(R.id.spinner_level);
        genderSpn = findViewById(R.id.spinner_gender);
        phoneEdt = findViewById(R.id.edt_phone_input);
        ageEdt = findViewById(R.id.edt_age_input);
        backBtn = findViewById(R.id.imgbtn_back);
        btnBackground1 = findViewById(R.id.btn_background1);
        btnBackground2 = findViewById(R.id.btn_background2);
    }

    /**
     * Setup click listeners for role selection buttons.
     * Ensures only one role is selected at a time.
     */
    private void setupRoleButtons() {
        studentBtn.setOnClickListener(v -> {                   // Set click listener on student button
            studentBtn.setSelected(true);                      // Mark student button as selected
            guideBtn.setSelected(false);                       // Mark guide button as not selected
        });

        guideBtn.setOnClickListener(v -> {                     // Set click listener on guide button
            guideBtn.setSelected(true);                        // Mark guide button as selected
            studentBtn.setSelected(false);                     // Mark student button as not selected
        });
    }

    /**
     * Setup input validation listeners and buttons' click listeners.
     * Includes phone and age validation, save, cancel and back buttons.
     */
    private void setupListeners() {
        phoneEdt.addTextChangedListener(new TextWatcher() {    // Add text change listener to phone input
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                validatePhoneInput(s.toString());              // Validate phone number after text changes
            }
        });

        ageEdt.addTextChangedListener(new TextWatcher() {      // Add text change listener to age input
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                validateAgeInput(s.toString());                // Validate age after text changes
            }
        });

        btnBackground1.setOnClickListener(v -> {                // Set click listener on background1 button
            selectedBackground = 1;                             // Set selected background to 1
            updateBackgroundButtonsUI();                        // Update UI to show selection
            setAppBackground(selectedBackground);               // Apply selected background to app
        });

        btnBackground2.setOnClickListener(v -> {                // Set click listener on background2 button
            selectedBackground = 2;                             // Set selected background to 2
            updateBackgroundButtonsUI();                        // Update UI to show selection
            setAppBackground(selectedBackground);               // Apply selected background to app
        });

        saveBtn.setOnClickListener(v -> handleSave());          // Set click listener for save button

        cancelBtn.setOnClickListener(v -> goToLessonsList());   // Cancel button: go back without saving
        backBtn.setOnClickListener(v -> goToLessonsList());     // Back button: go back without saving
    }


    /**
     * Validates the phone number input.
     * The phone must start with '05' and be exactly 10 digits.
     * Shows an error message if invalid, clears error if valid or empty.
     *
     * @param phone The input phone number string.
     */
    private void validatePhoneInput(String phone) {
        if (phone.isEmpty()) {
            phoneEdt.setError(null);  // No error if empty
            return;
        }

        if (!phone.matches("^05\\d{8}$")) {
            phoneEdt.setError("מס׳ ישראלי לא תקין (חייב להתחיל ב05 ולכלול 10 ספרות)");
        } else {
            phoneEdt.setError(null);  // Clear error if valid
        }
    }

    /**
     * Validates the age input.
     * Age must be between 14 and 120.
     * Shows error if invalid or not a number, clears error if valid or empty.
     *
     * @param ageStr The input age string.
     */
    private void validateAgeInput(String ageStr) {
        if (ageStr.isEmpty()) {
            ageEdt.setError(null);  // Empty age is allowed (optional field)
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);                 // Parse age string to int
            if (age < 14) {
                ageEdt.setError("הגיל צריך להיות מעל 14");      // Show error if age less than 14
            } else if (age > 120) {
                ageEdt.setError("הגיל צריך להיות מתחת ל120");   // Show error if age greater than 120
            } else {
                ageEdt.setError(null);                            // Clear error if age is valid
            }
        } catch (NumberFormatException e) {
            ageEdt.setError("שדה הגיל מקבל רק מספרים");         // Show error if age is not a number
        }
    }

    /**
     * Updates thr Button selection and adding stroke
     * **/
    private void updateBackgroundButtonsUI() {
        if (selectedBackground == 1) {                           // If background 1 selected
            btnBackground1.setSelected(true);                    // Set background1 button selected state
            btnBackground1.setStrokeColor(getColorStateList(R.color.black)); // Set stroke color to black
            btnBackground1.setStrokeWidth(6);                    // Set stroke width to 6
            btnBackground2.setSelected(false);                   // Deselect background2 button - So only one button is selected
            btnBackground2.setStrokeWidth(0);                    // Remove stroke on background2
        } else {
            btnBackground2.setSelected(true);                    // Else select background2 button
            btnBackground2.setStrokeColor(getColorStateList(R.color.black)); // Set stroke color black
            btnBackground2.setStrokeWidth(6);                    // Set stroke width to 6
            btnBackground1.setSelected(false);                   // Deselect background1 button - So only one button is selected
            btnBackground1.setStrokeWidth(0);                    // Remove stroke on background1
        }
    }

    /**
     * Setting Up Background App by int (1 or 2) 1 to background1 and vise versa
     * **/
    private void setAppBackground(int bgChoice) {
        findViewById(R.id.rootLayout)                       // Find root view of activity
                .setBackgroundResource(                          // Set background resource
                        bgChoice == 1 ? R.drawable.background1 : R.drawable.background2); // Choose drawable based on choice
    }

    /**
     * Handles the save button click event.
     * Checks if any fields changed, validates inputs, updates user data via ViewModel,
     * sets result accordingly and finishes the activity.
     */
    private void handleSave() {
        if (hasInputErrors()) {                                  // If input fields have errors
            showInputErrorsToast();                              // Show error message Toast
            return;                                              // Stop saving
        }

        boolean hasChanges = updateUserFieldsIfChanged();       // Update user fields if changed and track if changed

        if (checkAndUpdateRoleChanges()) {                       // Check if role changed and update if needed
            hasChanges = true;                                   // Mark that there are changes
        }

        if (checkAndUpdateBackgroundChanges()) {                 // Check if background changed and update if needed
            hasChanges = true;                                   // Mark that there are changes
        }

        if (hasChanges) {                                        // If there were any changes
            userViewModel.updateUser(currentUser);              // Update user in database
            saveBackgroundPref();                                // Save background preference
            Intent resultIntent = new Intent();                  // Prepare intent for result
            resultIntent.putExtra("updated", true);              // Put boolean extra indicating update
            setResult(RESULT_OK, resultIntent);                  // Set result OK with intent
        } else {
            setResult(RESULT_CANCELED);                          // Otherwise set result canceled (no changes)
        }

        finish();                                               // Close the activity
    }

    /**
     * Checking if Phone input has errors or the age input
     *
     * @return true if there has been any errors
     */
    private boolean hasInputErrors() {
        return phoneEdt.getError() != null || ageEdt.getError() != null;
    }

    /**
     * Show error message according to the specific error
     */
    private void showInputErrorsToast() {
        boolean phoneHasError = phoneEdt.getError() != null;    // Check if phone field has error
        boolean ageHasError = ageEdt.getError() != null;        // Check if age field has error

        if (phoneHasError && ageHasError) {                     // Both phone and age invalid
            Toast.makeText(this, "יש לתקן את שדות הטלפון והגיל לפני השמירה", Toast.LENGTH_LONG).show();
        } else if (phoneHasError) {                              // Only phone invalid
            Toast.makeText(this, "יש לתקן את שדה הטלפון לפני השמירה", Toast.LENGTH_LONG).show();
        } else {                                                 // Only age invalid
            Toast.makeText(this, "יש לתקן את שדה הגיל לפני השמירה", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check and Update the Fields
     *
     * @return true if there has been any changes
     */
    private boolean updateUserFieldsIfChanged() {
        boolean hasChanges = false;                              // Track if any field changed

        String selectedGender = genderSpn.getSelectedItem().toString(); // Get selected gender string
        String selectedLevel = levelSpn.getSelectedItem().toString();   // Get selected level string
        String enteredPhone = phoneEdt.getText().toString().trim();     // Get entered phone string
        String enteredAgeStr = ageEdt.getText().toString().trim();      // Get entered age string

        if (currentUser.getGender() == null || !currentUser.getGender().equals(selectedGender)) {
            currentUser.setGender(selectedGender);              // Update gender if changed or null
            hasChanges = true;                                   // Mark changes
        }

        if (currentUser.getLevel() == null || !currentUser.getLevel().equals(selectedLevel)) {
            currentUser.setLevel(selectedLevel);                 // Update level if changed or null
            hasChanges = true;                                   // Mark changes
        }

        if (currentUser.getPhoneNumber() == null || !currentUser.getPhoneNumber().equals(enteredPhone)) {
            currentUser.setPhoneNumber(enteredPhone);            // Update phone if changed or null
            hasChanges = true;                                   // Mark changes
        }

        if (!enteredAgeStr.isEmpty()) {                          // If age field not empty
            try {
                int enteredAge = Integer.parseInt(enteredAgeStr); // Parse age to int
                if (currentUser.getAge() != enteredAge) {
                    currentUser.setAge(enteredAge);              // Update age if changed
                    hasChanges = true;                           // Mark changes
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "שדה הגיל מקבל רק מספרים", Toast.LENGTH_LONG).show(); // Show error if invalid number
                return false;                                    // Stop saving
            }
        } else {                                                 // Age field empty
            if (currentUser.getAge() != 0) {                     // If current age not zero
                currentUser.setAge(0);                            // Reset age to zero
                hasChanges = true;                               // Mark changes
            }
        }

        return hasChanges;                                       // Return if any changes happened
    }

    /**
     * Check and Update changes in Role
     *
     * @return true if there has been any changes
     */
    private boolean checkAndUpdateRoleChanges() {
        boolean hasChanges = false;                              // Track if role changed

        if (currentUser.getRole().equals("student") && guideBtn.isSelected()) {
            currentUser.setRole("guide");                        // Change role from student to guide
            hasChanges = true;                                   // Mark changes
            setFirstTimePref();                                  // Set first_time flag in prefs
        }

        if (currentUser.getRole().equals("guide") && studentBtn.isSelected()) {
            currentUser.setRole("student");                      // Change role from guide to student
            hasChanges = true;                                   // Mark changes
            setFirstTimePref();                                  // Set first_time flag in prefs
        }

        return hasChanges;                                       // Return if role changed
    }

    /**
     * Check and Update Changes in background
     *
     * @return true if there has been any changes
     */
    private boolean checkAndUpdateBackgroundChanges() {
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE); // Load prefs
        int savedBackground = prefs.getInt(KEY_BACKGROUND, 1);   // Get saved background
        int selectedNumBack = btnBackground1.isSelected() ? 1 : 2; // Get selected background number

        if (savedBackground != selectedNumBack) {                 // If background changed
            selectedBackground = selectedNumBack;                 // Update selectedBackground variable
            return true;                                          // Indicate change happened
        }
        return false;                                             // No change
    }


    /**
     * Saving Background pref in SharedPreferences
     * **/
    private void saveBackgroundPref() {
        SharedPreferences.Editor editor = getSharedPreferences("SugarStepsPref", MODE_PRIVATE).edit(); // Get editor
        editor.putInt(KEY_BACKGROUND, selectedBackground);     // Save selected background number
        editor.apply();                                         // Commit changes asynchronously
    }


    /**
     * Helper method to set the "first_time" preference to true
     * This can is used to trigger popup on next activity launch.
     */
    private void setFirstTimePref() {
        SharedPreferences.Editor editor = getSharedPreferences("SugarStepsPref", MODE_PRIVATE).edit(); // Get editor
        editor.putBoolean("first_time", true);                 // Save first_time flag as true
        editor.apply();                                         // Commit changes asynchronously
    }

    /**
     * Loads the user ID from shared preferences.
     *
     * @return the stored user ID or -1 if not found
     */
    private long loadUserIdFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE); // Load prefs
        return prefs.getLong("userId", -1);                      // Return saved user ID or -1 if none
    }

    /**
     * Observes user data from the database using ViewModel.
     * When user data changes, updates the UI accordingly.
     */
    private void observeUserData() {
        long userId = loadUserIdFromPrefs();                     // Get user ID from prefs
        userViewModel.getUserById(userId).observe(this, this::populateUserFields); // Observe user data and populate UI
    }

    /**
     * Populates UI fields with the user data.
     *
     * @param user the user object fetched from DB
     */
    private void populateUserFields(User user) {
        currentUser = user;                                       // Store current user object
        updateRoleUI(user.getRole());                             // Update role buttons UI based on user role
        updateAgeUI(user.getAge());                               // Update age field with user age
        updatePhoneUI(user.getPhoneNumber());                     // Update phone field with user phone
        updateSpinnerSelection(levelSpn, R.array.levels, user.getLevel(), "מתחילים"); // Select user level in spinner (skip "מתחילים")
        updateSpinnerSelection(genderSpn, R.array.gender, user.getGender(), null);    // Select gender in spinner

        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE); // Load prefs
        selectedBackground = prefs.getInt(KEY_BACKGROUND, 1);    // Load selected background
        updateBackgroundButtonsUI();                              // Update background buttons UI
        setAppBackground(selectedBackground);                     // Set background image
    }

    /**
     * Updates role buttons UI based on current role.
     *
     * @param role current role ("student" or "guide")
     */
    private void updateRoleUI(String role) {
        boolean isGuide = "guide".equals(role);                   // Check if role is guide
        guideBtn.setSelected(isGuide);                            // Select guide button if true
        studentBtn.setSelected(!isGuide);                         // Select student button if not guide
    }

    /**
     * Sets the age input field with user's age if it is not zero.
     *
     * @param age the user's age
     */
    private void updateAgeUI(int age) {
        if (age != 0) {
            ageEdt.setText(String.valueOf(age)); // Set age EditText text if age input not null
        }
    }

    /**
     * Sets the phone input field with user's phone number if not null.
     *
     * @param phone the user's phone number
     */
    private void updatePhoneUI(String phone) {
        if (phone != null) {
            phoneEdt.setText(phone); // Set phone EditText text if phone input not null
        }
    }

    /**
     * Sets the spinner selection based on the stored value.
     * Skips default value if specified.
     *
     * @param spinner      the Spinner view to update
     * @param arrayRes     the string-array resource id
     * @param value        the current user value to select
     * @param defaultValue a default value to ignore (optional)
     */
    private void updateSpinnerSelection(Spinner spinner, int arrayRes, String value, String defaultValue) {
        if (value != null && (defaultValue == null || !value.equals(defaultValue))) { // If value valid and not default
            String[] array = getResources().getStringArray(arrayRes);                // Load string array for spinner options
            int index = getSelectedIndex(array, value);                             // Find index of value
            if (index >= 0) {                                                       // If found
                spinner.setSelection(index);                                        // Set spinner selection
            }
        }
    }

    /**
     * Finds the index of the desiredItem in the given array.
     *
     * @param arr         array of strings
     * @param desiredItem the string to find
     * @return index of desiredItem or -1 if not found
     */
    private int getSelectedIndex(String[] arr, String desiredItem) {
        for (int i = 0; i < arr.length; i++) {                     // Iterate array items
            if (arr[i].equals(desiredItem)) {                      // Check for match
                return i;                                           // Return index if found
            }
        }
        return -1;                                                  // Return -1 if not found
    }

    /**
     * Exits the activity without saving changes and returns RESULT_CANCELED.
     */
    private void goToLessonsList() {
        setResult(RESULT_CANCELED);                                 // Set result canceled for calling activity
        finish();                                                  // Close current activity
    }
}
