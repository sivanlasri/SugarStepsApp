package com.example.sugarsteps.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sugarsteps.lesson.LessonsListActivity;
import com.example.sugarsteps.R;
import com.google.android.material.button.MaterialButton;

public class RegistrationActivity extends AppCompatActivity {

    private EditText usernameEt;
    private MaterialButton studentBtn, guideBtn;
    private CheckBox responsibleChk;
    private TextView warningText;
    private MaterialButton continueBtn;

    private UserViewModel userViewModel; // ViewModel instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize UI components
        usernameEt = findViewById(R.id.et_username_text);
        studentBtn = findViewById(R.id.btn_registration_student);
        guideBtn = findViewById(R.id.btn_registration_guide);
        responsibleChk = findViewById(R.id.chk_declaration);
        warningText = findViewById(R.id.tv_warning_cant_continue);
        continueBtn = findViewById(R.id.btn_lets_go);

        continueBtn.setEnabled(false);

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe the LiveData that contains the inserted user's ID
        userViewModel.getInsertedUserId().observe(this, id -> {

            // Check if the ID is not null and not -1 (meaning the insert was successful)
            if (id != null && id != -1) {

                // Access SharedPreferences to store the user ID locally
                SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);

                // Save the user ID in SharedPreferences using the key "userId"
                prefs.edit().putLong("userId", id).apply();

                // Create an intent to navigate from RegistrationActivity to LessonsListActivity activity
                Intent intent = new Intent(RegistrationActivity.this, LessonsListActivity.class);

                // Start the LessonsListActivity activity
                startActivity(intent);

                // Close the current RegistrationActivity so the user can't go back to it
                finish();
            }
        });

        // Role buttons logic
        studentBtn.setOnClickListener(v -> {
            studentBtn.setSelected(true);
            guideBtn.setSelected(false);
            updateContinueButtonState();
        });

        guideBtn.setOnClickListener(v -> {
            guideBtn.setSelected(true);
            studentBtn.setSelected(false);
            updateContinueButtonState();
        });

        // Responsibility CheckBox Logic
        responsibleChk.setOnClickListener(v -> {
            responsibleChk.setChecked(false);

            // Setting view popup
            View popupView = getLayoutInflater().inflate(R.layout.popup_dec, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(RegistrationActivity.this)
                    .setView(popupView)
                    .setCancelable(false)
                    .create();

            // Setting buttons layout
            MaterialButton btnConfirm = popupView.findViewById(R.id.btn_confirm);
            MaterialButton btnCancel = popupView.findViewById(R.id.btn_cancel);

            // Confirm Button Logic
            btnConfirm.setOnClickListener(v1 -> {
                responsibleChk.setChecked(true); // Check the checkbox
                warningText.setVisibility(View.GONE); // Visibility Gone for warning
                warningText.setText(""); // Deleting warning
                updateContinueButtonState(); // Check for setting enable the button continue
                dialog.dismiss();
            });

            // Cancel Button Logic
            btnCancel.setOnClickListener(v1 -> {
                responsibleChk.setChecked(false); // UnCheck the checkbox
                warningText.setVisibility(View.VISIBLE); // Visibility Visible for warning
                warningText.setText("חייב לקבל את התנאים כדי להמשיך."); // Setting warning text
                updateContinueButtonState(); // Check for setting enable the button continue
                dialog.dismiss();
            });

            dialog.show(); // Showing the text in pop up

            // Set custom dialog appearance
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                int width = (int) (getResources().getDisplayMetrics().density * 350);
                dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });

        // Listen to username changes
        usernameEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState(); // Re-check all conditions
            }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });

        // Continue button click
        continueBtn.setOnClickListener(v -> {
            String username = usernameEt.getText() != null ? usernameEt.getText().toString().trim() : "";

            // Validate inputs
            if (username.isEmpty()) {
                warningText.setVisibility(View.VISIBLE);
                warningText.setText("יש להזין שם משתמש.");
                return;
            }

            if (!responsibleChk.isChecked()) {
                warningText.setVisibility(View.VISIBLE);
                warningText.setText("חייב לקבל את התנאים כדי להמשיך.");
                return;
            }

            if (!studentBtn.isSelected() && !guideBtn.isSelected()) {
                warningText.setVisibility(View.VISIBLE);
                warningText.setText("יש לבחור תפקיד.");
                return;
            }

            // Hide warning
            warningText.setVisibility(View.GONE);

            // Determine user role
            String role = studentBtn.isSelected() ? "student" : "guide";
            User newUser = new User(username, role);

            // Insert user through ViewModel (async)
            userViewModel.insert(newUser);

            // Save registration status immediately (you can move this to observer if you want)
            SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isRegistered", true);
            editor.apply();

        });
    }

    // Checks all conditions and enables/disables the continue button accordingly.
    private void updateContinueButtonState() {
        String username = usernameEt.getText() != null ? usernameEt.getText().toString().trim() : "";
        boolean hasUsername = !username.isEmpty();
        boolean hasRole = studentBtn.isSelected() || guideBtn.isSelected();
        boolean hasChecked = responsibleChk.isChecked();

        if (hasUsername && hasRole && hasChecked) {
            continueBtn.setEnabled(true);
            continueBtn.setBackgroundResource(R.drawable.button_go);
            warningText.setVisibility(View.GONE);
        } else {
            continueBtn.setEnabled(false);
            continueBtn.setBackgroundResource(R.drawable.button_add);
        }
    }
}
