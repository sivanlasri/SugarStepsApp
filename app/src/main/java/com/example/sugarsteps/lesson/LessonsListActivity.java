package com.example.sugarsteps.lesson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sugarsteps.R;
import com.example.sugarsteps.user.SettingsActivity;
import com.example.sugarsteps.user.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LessonsListActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> settingsActivityLauncher;

    // UI components
    private TextView helloUserTv;             // TextView to greet the user
    private FloatingActionButton addFab;      // FloatingActionButton to add new lesson (visible only to guides)
    private RecyclerView recyclerView;        // RecyclerView to display lessons list
    private LessonAdapter lessonAdapter;      // Adapter for RecyclerView to bind lessons data
    private TabLayout tabLayout;              // TabLayout for filtering lessons by level
    private ImageView menuImgBtn;           // Menu button to open up
    private ImageButton infoBtn;            // Info button

    // Data structures
    private List<Lesson> allLessons = new ArrayList<>();       // List containing all lessons
    private List<Lesson> filteredLessons = new ArrayList<>();  // List filtered by selected level

    // ViewModel
    private UserViewModel userViewModel;  // ViewModel to access user data asynchronously

    private int selectedBackground = 1;  // Selected background ID loaded from preferences, default 1


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lessons_list);  // Set layout for the activity

        initViews();  // Initialize all UI views
        setupActivityResultLauncher();  // Setup result launcher to handle returned data from other activities
        loadSelectedBackground();  // Load selected background ID from SharedPreferences
        applyBackground();  // Apply background image to root layout based on selectedBackground

        setupRecyclerView();  // Setup RecyclerView with adapter and layout manager
        setupViewModel();  // Initialize ViewModels and observe lessons data
        setupTabs();  // Initialize TabLayout with custom tabs and iconsס
        setupListeners();  // Setup event listeners for UI components

        loadUserFromPrefs();  // Load user data from SharedPreferences and update UI accordingly

        checkFirstTimeAndShowPopup(); // Check first time - to show pop up
    }

    /**
     * Shows a welcome popup if this is the user's first time in the app.
     *
     * Uses the "first_time" flag from SharedPreferences. If true:
     * - Loads the user by ID.
     * - Displays a role-based popup.
     * - Updates the flag to false so it's not shown again.
     */
    private void checkFirstTimeAndShowPopup() {
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("first_time", true);

        if (isFirstTime) // Check first time
        {
            long userId = prefs.getLong("userId", -1);
            userViewModel.getUserById(userId).observe(this, user -> {
                if (user != null) {
                    showPopup(user.getRole());
                    // Update not first time run
                    prefs.edit().putBoolean("first_time", false).apply();
                }
            });




        }
    }

    /**
     * Finds and initializes all UI components by their IDs.
     */
    private void initViews() {
        // Find views by ID
        helloUserTv = findViewById(R.id.tv_hello_user);  // Initialize greeting TextView
        addFab = findViewById(R.id.fab_add);             // Initialize add button
        recyclerView = findViewById(R.id.recyclerview);  // Initialize RecyclerView
        tabLayout = findViewById(R.id.tl_headers);       // Initialize TabLayout
        menuImgBtn = findViewById(R.id.icon_settings);   // Initialize Icon settings
        infoBtn = findViewById(R.id.imgbtn_info); // Initialize Info icon
    }


    /**
     * Sets up the ActivityResultLauncher to handle results from activities like Settings and LessonAddEdit.
     */
    private void setupActivityResultLauncher() {
        // Register ActivityResultLauncher to receive results from launched activities (Settings, Lesson Add/Edit)
        settingsActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("updated", false)) {
                            // If data was updated in the child activity, reload user data and lessons
                            reloadUserData();
                            reloadLessons();
                            loadSelectedBackground();
                            applyBackground();
                        }
                    }
                }
        );
    }


    /**
     * Applies the selected background image to the root layout based on the stored preference.
     */
    private void applyBackground() {
        // Map the selected background ID to actual drawable resource and set it as background to the root layout
        int backgroundResId;
        switch (selectedBackground) {
            case 2:
                backgroundResId = R.drawable.background2;
                break;
            // Add more cases here if you have more background options
            case 1:
            default:
                backgroundResId = R.drawable.background1;
                break;
        }
        // The root layout must have the ID 'rootLayout' for this to work
        findViewById(R.id.rootLayout).setBackgroundResource(backgroundResId);
    }

    /**
     * Initializes the RecyclerView, sets its layout manager and adapter.
     */
    private void setupRecyclerView() {
        // Initialize the LessonAdapter with filtered lessons list
        lessonAdapter = new LessonAdapter(this, filteredLessons);

        // Set LinearLayoutManager for vertical scrolling
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Attach adapter to RecyclerView
        recyclerView.setAdapter(lessonAdapter);
    }


    /**
     * Initializes the UserViewModel and LessonsViewModel.
     * Observes lessons LiveData to update the lessons list and filter by selected tab.
     */
    private void setupViewModel() {
        // Initialize UserViewModel to access user data
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Initialize LessonsViewModel and observe all lessons data
        LessonsViewModel lessonsViewModel = new ViewModelProvider(this).get(LessonsViewModel.class);
        lessonsViewModel.getAllLessons().observe(this, lessons -> {
            if (lessons != null) {
                // Update the full lessons list when new data arrives
                allLessons.clear();
                allLessons.addAll(lessons);

                // Filter lessons by currently selected tab's level
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (selectedTab != null && selectedTab.getCustomView() != null) {
                    TextView tabText = selectedTab.getCustomView().findViewById(R.id.tab_text);
                    if (tabText != null) {
                        filterLessonsByLevel(tabText.getText().toString());
                    }
                }
            }
        });
    }


    /**
     * Sets listeners for UI components including tab selection and button clicks.
     */
    private void setupListeners() {
        // Listen for tab selection changes to update styles and filter lessons
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                updateTabStyle(tab, true);
                String selectedLevel = "";
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabText = customView.findViewById(R.id.tab_text);
                    if (tabText != null) selectedLevel = tabText.getText().toString();
                }
                filterLessonsByLevel(selectedLevel);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {
                updateTabStyle(tab, false);
            }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                // No action needed on reselect
            }
        });

        // Setup click listener on the settings menu icon
        menuImgBtn.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.getMenuInflater().inflate(R.menu.menu_settings, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_settings) {
                    // Launch SettingsActivity via result launcher
                    Intent intent = new Intent(LessonsListActivity.this, SettingsActivity.class);
                    settingsActivityLauncher.launch(intent);
                    return true;
                }
                return false;
            });
            menu.show();
        });

        // Setup click listener on the add floating action button
        addFab.setOnClickListener(v -> {
            Intent intent = new Intent(LessonsListActivity.this, LessonAddEditActivity.class);
            intent.putExtra("mode", "add");  // Specify mode as "add" for adding new lesson
            settingsActivityLauncher.launch(intent);
        });
    }

    /**
     * Loads the userId from SharedPreferences and calls loadUserData with it.
     */
    private void loadUserFromPrefs() {
        // Load user ID from SharedPreferences and load user data from ViewModel
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);
        loadUserData(userId);
    }


    /**
     * Loads the selected background ID from SharedPreferences.
     */
    private void loadSelectedBackground() {
        // Load the selected background ID from SharedPreferences using the key "selected_background"
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
        selectedBackground = prefs.getInt("selected_background", 1);  // Default to 1 if not found
    }

    /**
     * Reloads the user data from ViewModel and updates UI accordingly.
     * Used when data might have changed (after returning from Settings or Lesson add/edit).
     */
    private void reloadUserData()
    {
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE); // Getting Shared Preferences
        long userId = prefs.getLong("userId", -1); // Getting ID
        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {

                // If add fab isn't found - reset it
                if (addFab == null) {
                    addFab = findViewById(R.id.fab_add);
                }

                // Same logic as in On create
                helloUserTv.setText("שלום, " + user.getUsername());

                if ("guide".equals(user.getRole())) {
                    addFab.setVisibility(View.VISIBLE);
                } else {
                    addFab.setVisibility(View.GONE);
                }

                // Taking care of the tab according User's level
                String level = user.getLevel();
                int tabIndex = 0;
                if ("מתקדמים".equals(level)) tabIndex = 1;
                else if ("מומחים".equals(level)) tabIndex = 2;

                // Automatic selecting of the rigth Tab
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabIndex);
                if (selectedTab != null) {
                    tabLayout.selectTab(selectedTab);
                    updateTabStyle(selectedTab, true);
                    filterLessonsByLevel(level);
                }
            }
        });
    }

    /**
     * Sets up the TabLayout with custom tabs: beginner, advanced, expert, each with icon and text.
     */
    private void setupTabs() {
        tabLayout.removeAllTabs();  // Clear any existing tabs

        int[] icons = {
                R.drawable.ic_beginner,
                R.drawable.ic_advanced,
                R.drawable.ic_expert
        };  // Icons for each tab

        String[] tabTitles = {
                getString(R.string.beginner),
                getString(R.string.advanced),
                getString(R.string.expert)
        };  // Titles for each tab

        // Create tabs with custom views
        for (int i = 0; i < tabTitles.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();  // Create new tab
            View customTab = getLayoutInflater().inflate(R.layout.tab_custom, null);  // Inflate custom tab layout

            TextView tabText = customTab.findViewById(R.id.tab_text);  // Find text view in custom tab
            ImageView tabIcon = customTab.findViewById(R.id.tab_icon);  // Find icon view in custom tab

            tabText.setText(tabTitles[i]);    // Set tab title text
            tabIcon.setImageResource(icons[i]);  // Set tab icon image

            tab.setCustomView(customTab);  // Assign custom view to tab
            tabLayout.addTab(tab);         // Add tab to TabLayout
        }
    }

    /**
     * Updates the visual style of a tab depending on whether it's selected or not.
     *
     * @param tab        The tab to update
     * @param isSelected Whether the tab is currently selected
     */
    private void updateTabStyle(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();  // Get custom view of tab
        if (customView == null) return;          // Return if no custom view

        TextView tabText = customView.findViewById(R.id.tab_text);  // Find text view
        if (tabText == null) return;                                // Return if missing text view

        if (isSelected) {
            tabText.setTypeface(null, Typeface.BOLD);  // Bold font if selected
            tabText.setTextColor(ContextCompat.getColor(this, R.color.dark_peach));  // Highlight color
        } else {
            tabText.setTypeface(null, Typeface.NORMAL);  // Normal font if not selected
            tabText.setTextColor(ContextCompat.getColor(this, R.color.black));  // Default color
        }
    }

    /**
     * Filters the lesson list by the provided level and updates the RecyclerView adapter.
     *
     * @param level The level to filter lessons by (e.g., "Beginner", "Advanced", "Expert")
     */
    private void filterLessonsByLevel(String level) {
        filteredLessons.clear();  // Clear currently displayed lessons

        for (Lesson lesson : allLessons) {
            if (lesson.getLevel().equalsIgnoreCase(level)) {  // Compare level ignoring case
                filteredLessons.add(lesson);                    // Add lesson if level matches
            }
        }
        lessonAdapter.notifyDataSetChanged();  // Notify adapter about data change to refresh UI
    }

    /**
     * Loads the user data by userId and updates the UI accordingly.
     * Also sets up swipe-to-delete and long-press-to-edit if the user is a guide.
     *
     * @param userId The id of the user to load
     */
    private void loadUserData(long userId) {
        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                helloUserTv.setText("שלום, " + user.getUsername());
                addFab.setVisibility("guide".equals(user.getRole()) ? View.VISIBLE : View.GONE);

                String level = user.getLevel();
                int tabIndex = getTabIndexByLevel(level);

                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabIndex);
                if (selectedTab != null) {
                    tabLayout.selectTab(selectedTab);
                    updateTabStyle(selectedTab, true);
                    filterLessonsByLevel(level);
                }
                setupInfoButton(user.getRole()); // Showing pop up message according role

                if ("guide".equals(user.getRole())) {
                    // Swipe to delete
                    ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView,
                                              @NonNull RecyclerView.ViewHolder viewHolder,
                                              @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            int position = viewHolder.getAdapterPosition();
                            Lesson lessonToDelete = lessonAdapter.getLessonAt(position);
                            LessonsViewModel viewModel = new ViewModelProvider(LessonsListActivity.this).get(LessonsViewModel.class);
                            viewModel.deleteLesson(lessonToDelete);
                            Toast.makeText(LessonsListActivity.this, "השיעור נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                        }
                    };

                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
                    itemTouchHelper.attachToRecyclerView(recyclerView);

                    // Long press to edit
                    lessonAdapter.setOnItemLongClickListener(lessonId -> {
                        Intent intent = new Intent(LessonsListActivity.this, LessonAddEditActivity.class);
                        intent.putExtra("mode", "edit");
                        intent.putExtra("lessonId", lessonId);
                        settingsActivityLauncher.launch(intent);
                    });
                }

            }
        });

    }

    /**
     * Reloads lessons data from the database and updates the list and filter.
     */
    private void reloadLessons() {
        // Create or get an instance of LessonsViewModel to access lesson data
        LessonsViewModel lessonsViewModel = new ViewModelProvider(this).get(LessonsViewModel.class);

        // Observe the LiveData containing all lessons from the database
        lessonsViewModel.getAllLessons().observe(this, lessons -> {
            if (lessons != null) {
                // Clear the current list of all lessons to prepare for fresh data
                allLessons.clear();

                // Add all the newly fetched lessons to the allLessons list
                allLessons.addAll(lessons);

                // Get the currently selected tab from the TabLayout
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());

                // Check if a tab is selected and it has a custom view set
                if (selectedTab != null && selectedTab.getCustomView() != null) {
                    // Find the TextView inside the custom tab view that contains the tab's title
                    TextView tabText = selectedTab.getCustomView().findViewById(R.id.tab_text);

                    if (tabText != null) {
                        // Use the tab's title text to filter the lessons by the selected level
                        filterLessonsByLevel(tabText.getText().toString());
                    }
                }
            }
        });
    }


    /**
     * Returns the tab index corresponding to the user level.
     *
     * @param level User level string (Hebrew): "מתקדמים", "מומחים", or default
     * @return Tab index (0 = beginner, 1 = advanced, 2 = expert)
     */
    private int getTabIndexByLevel(String level) {
        switch (level) {
            case "מתקדמים": return 1;
            case "מומחים": return 2;
            default: return 0;
        }
    }

    /**
     * Displays a popup dialog with a welcome message according to the user's role.
     *
     * @param role User role ("guide" or "student")
     */
    private void showPopup (String role)
    {
        View popupView = null;
        // Checking the role and costume the Layout Accordingly
        if ("guide".equals(role)){
            popupView = LayoutInflater.from(this).inflate(R.layout.welcome_guide, null); // Loading pop up guide
        } else if ("student".equals(role)) {
            popupView = LayoutInflater.from(this).inflate(R.layout.welcome_student, null); // Loading pop up guide
        }
        // Checking that the view is not null
        if(popupView!=null){
            ImageButton closeButton = popupView.findViewById(R.id.btn_close); // Finding close button
            // Creating the popup
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(popupView)
                    .setCancelable(false)
                    .create();
            // Setting closing operation
            closeButton.setOnClickListener(v -> dialog.dismiss()); // When clicking it closes the dialog
            if (!isFinishing() && !isDestroyed()) {
                dialog.show(); // Showing popup
            }

        }
    }

    /**
     * Sets the Info button click listener to show the popup according to the user's role.
     *
     * @param role User role
     */
    private void setupInfoButton(String role) {
        infoBtn.setOnClickListener(v -> showPopup(role));
    }


}

