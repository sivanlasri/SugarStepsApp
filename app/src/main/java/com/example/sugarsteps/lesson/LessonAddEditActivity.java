package com.example.sugarsteps.lesson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sugarsteps.R;
import com.example.sugarsteps.user.UserViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Activity for adding and editing lessons in the SugarSteps application.
 * Handles both creation of new lessons and editing existing ones.
 *
 * Features:
 * - Add/Edit lesson with name, description, video, image, and text file
 * - File validation and permission handling
 * - Media playback with proper resource management
 * - Background theme support from user preferences
 *
 * @author Sivan Lasri
 * @version 25.0
 */
public class LessonAddEditActivity extends AppCompatActivity {

    // Constants for SharedPreferences and configuration
    private static final String SHARED_PREFS_NAME = "SugarStepsPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_UPDATED = "updated";
    private static final String MODE_EDIT = "edit";
    private static final String MODE_ADD = "add";
    private static final int MAX_LESSON_NAME_LENGTH = 20;
    private static final int MAX_SHORT_DESC_LENGTH = 29;
    private static final int SAVE_DELAY_MS = 100;

    // ViewModels for database operations
    private UserViewModel userViewModel;
    private LessonsViewModel lessonsViewModel;

    // UI Components
    private ImageButton backBtn, addPhotoBtn;
    private EditText lessonNameEt, shortDescEt, lessonGuideEt;
    private Spinner levelSpn;
    private MaterialButton addVideoBtn, addLongDescBtn, saveBtn, cancelBtn;
    private VideoView lessonVideo;
    private TextView lessonLongDescTx;
    private MediaController mediaController;
    private View rootLayout; // Reference to root layout view for dynamic background

    // Activity Result Launchers for file picking and permissions
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> videoPermissionLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;
    private ActivityResultLauncher<Intent> textFilePickerLauncher;
    private ActivityResultLauncher<String> textPermissionLauncher;

    // Data storage for selected files and activity state
    private Uri videoUri = null;
    private Uri selectedImageUri = null;
    private Uri fileUri = null;
    private String mode;
    private int lessonId;
    private boolean isSaving = false;
    private boolean toastShown = false;
    private Lesson currentLesson = null;
    private int selectedBackground = 1; // Default background ID

    // File management flags
    private boolean isResourceFile = false; // Flag to identify resource files
    private String originalImagePath = null; // Original image file path
    private String originalVideoPath = null; // Original video file path
    private String originalTextPath = null; // Original text file path

    // User selection tracking flags
    private boolean userSelectedNewImage = false; // Track if user selected new image
    private boolean userSelectedNewVideo = false; // Track if user selected new video
    private boolean userSelectedNewText = false; // Track if user selected new text file

    /**
     * Called when the activity is first created.
     * Initializes UI components, ViewModels, and sets up all event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after being shut down,
     *                          this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lesson);

        // Initialize core components
        initializeIntentData();
        initializeViewModels();
        initializeUIComponents();

        // Load and apply background theme from SharedPreferences
        loadBackgroundFromPreferences();

        // Setup functionality
        setupMediaController();
        loadUserGuide();
        loadLessonDataIfEditMode();

        // Initialize launchers and listeners
        initializeActivityResultLaunchers();
        setupTextWatchers();
        setupClickListeners();
    }

    /**
     * Initialize intent data and determine activity mode (ADD or EDIT).
     */
    private void initializeIntentData() {
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        lessonId = intent.getIntExtra("lessonId", -1);
    }

    /**
     * Initialize ViewModels for database operations.
     */
    private void initializeViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        lessonsViewModel = new ViewModelProvider(this).get(LessonsViewModel.class);
    }

    /**
     * Initialize all UI components by finding views in the layout.
     */
    private void initializeUIComponents() {
        // Find and initialize all UI components
        backBtn = findViewById(R.id.imgbtn_back);
        addPhotoBtn = findViewById(R.id.imgbtn_add_photo);
        lessonNameEt = findViewById(R.id.et_lesson_name);
        shortDescEt = findViewById(R.id.et_lesson_short_desc);
        lessonGuideEt = findViewById(R.id.et_guide_name);
        levelSpn = findViewById(R.id.spinner_level);
        addVideoBtn = findViewById(R.id.btn_add_video);
        addLongDescBtn = findViewById(R.id.btn_add_long_desc);
        saveBtn = findViewById(R.id.btn_save_lesson);
        cancelBtn = findViewById(R.id.btn_cancel_lesson);
        lessonVideo = findViewById(R.id.video_preview);
        lessonLongDescTx = findViewById(R.id.tx_lesson_long_desc);
        rootLayout = findViewById(R.id.root_layout); // Get root layout for background
    }

    /**
     * Load background theme from SharedPreferences and apply it to the layout.
     */
    private void loadBackgroundFromPreferences() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        // Get saved background ID or default to 1
        selectedBackground = prefs.getInt("selected_background", 1);
        applyBackground(selectedBackground); // Apply background to root layout
    }

    /**
     * Apply the selected background resource to the root layout.
     * @param backgroundId The selected background ID from preferences.
     */
    private void applyBackground(int backgroundId) {
        if (rootLayout == null) {
            // If specific root layout not found, apply to activity background
            int drawableResId = getBackgroundResource(backgroundId);
            getWindow().setBackgroundDrawableResource(drawableResId);
            return;
        }

        int drawableResId = getBackgroundResource(backgroundId);
        rootLayout.setBackgroundResource(drawableResId); // Set background drawable
    }

    /**
     * Get the appropriate background resource based on background ID.
     * @param backgroundId The background ID from preferences
     * @return The drawable resource ID for the background
     */
    private int getBackgroundResource(int backgroundId) {
        switch (backgroundId) {
            case 2:
                return R.drawable.background2; // Background 2 drawable resource
            default:
                return R.drawable.background1; // Default background drawable resource
        }
    }

    /**
     * Setup MediaController for video playback with proper anchoring.
     */
    private void setupMediaController() {
        mediaController = new MediaController(this);
        mediaController.setAnchorView(lessonVideo);
        lessonVideo.setMediaController(mediaController);
    }

    /**
     * Load lesson data if activity is in EDIT mode.
     */
    private void loadLessonDataIfEditMode() {
        if (MODE_EDIT.equals(mode) && lessonId != -1) {
            lessonsViewModel.getLessonById(lessonId).observe(this, lesson -> {
                if (lesson != null && !isSaving) {
                    currentLesson = lesson;
                    loadLessonData(lesson);
                }
            });
        }
    }

    /**
     * Initialize all ActivityResultLauncher objects for file picking and permissions.
     */
    private void initializeActivityResultLaunchers() {
        setupImagePickerLauncher();
        setupImagePermissionLauncher();
        setupVideoPermissionLauncher();
        setupVideoPickerLauncher();
        setupTextPermissionLauncher();
        setupTextFilePickerLauncher();
    }

    /**
     * Setup image picker launcher to handle image selection results.
     */
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        addPhotoBtn.setImageURI(selectedImageUri);
                        userSelectedNewImage = true; // Mark that user selected new image
                    }
                }
        );
    }

    /**
     * Setup image permission launcher to request READ permission for images.
     */
    private void setupImagePermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "לא ניתן לבחור תמונה בלי הרשאה", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Setup video permission launcher to request READ permission for videos.
     */
    private void setupVideoPermissionLauncher() {
        videoPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openVideoPicker();
                    } else {
                        Toast.makeText(this, "לא ניתן לבחור וידאו בלי הרשאה", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Setup video picker launcher to handle video selection results with persistent permissions.
     */
    private void setupVideoPickerLauncher() {
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        videoUri = result.getData().getData();
                        if (videoUri != null) {
                            handleVideoSelection(videoUri);
                        }
                    }
                }
        );
    }

    /**
     * Handle video selection with proper permission management and validation.
     * @param uri The selected video URI
     */
    private void handleVideoSelection(Uri uri) {
        try {
            getContentResolver().openInputStream(uri);
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setupVideoPlayer(uri);
            userSelectedNewVideo = true; // Mark that user selected new video
        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בגישה לקובץ הווידאו", Toast.LENGTH_SHORT).show();
            videoUri = null;
        }
    }

    /**
     * Setup text permission launcher to request READ permission for text files.
     */
    private void setupTextPermissionLauncher() {
        textPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openTextFilePicker();
                    } else {
                        Toast.makeText(this, "לא ניתן לגשת לקובץ טקסט ללא הרשאה", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Setup text file picker launcher to handle text file selection results.
     */
    private void setupTextFilePickerLauncher() {
        textFilePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        fileUri = result.getData().getData();
                        readTextFromUri(fileUri);
                        userSelectedNewText = true; // Mark that user selected new text
                    }
                }
        );
    }

    /**
     * Setup TextWatcher objects for input validation on EditText fields.
     */
    private void setupTextWatchers() {
        setupLessonNameTextWatcher();
        setupShortDescTextWatcher();
    }

    /**
     * Setup TextWatcher for lesson name field to enforce character limit.
     */
    private void setupLessonNameTextWatcher() {
        lessonNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (lessonNameEt.length() > MAX_LESSON_NAME_LENGTH) {
                    lessonNameEt.setError("שם השיעור צריך להיות עד " + MAX_LESSON_NAME_LENGTH + " תווים בלבד.");
                }
            }
        });
    }

    /**
     * Setup TextWatcher for short description field to enforce character limit.
     */
    private void setupShortDescTextWatcher() {
        shortDescEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (shortDescEt.length() > MAX_SHORT_DESC_LENGTH) {
                    shortDescEt.setError("התיאור הקצר צריך להיות עד " + MAX_SHORT_DESC_LENGTH + " תווים בלבד.");
                }
            }
        });
    }

    /**
     * Setup click listeners for all interactive UI components.
     */
    private void setupClickListeners() {
        setupNavigationButtons();
        setupMediaButtons();
        setupActionButtons();
    }

    /**
     * Setup click listeners for navigation buttons (back, cancel).
     */
    private void setupNavigationButtons() {
        // Back button - cancel operation and return to previous screen
        backBtn.setOnClickListener(v -> handleCancelOperation());

        // Cancel button - same as back button
        cancelBtn.setOnClickListener(v -> handleCancelOperation());
    }

    /**
     * Handle cancel operation for both back and cancel buttons.
     */
    private void handleCancelOperation() {
        isSaving = true;
        String message = MODE_EDIT.equals(mode) ? "עריכת השיעור בוטלה בהצלחה" : "הוספת השיעור בוטלה בהצלחה";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Setup click listeners for media-related buttons (photo, video, text).
     */
    private void setupMediaButtons() {
        // Add photo button - request permission and open image picker
        addPhotoBtn.setOnClickListener(v -> requestImagePermission());

        // Add video button - request permission and open video picker
        addVideoBtn.setOnClickListener(v -> requestVideoPermission());

        // Add long description button - request permission and open text file picker
        addLongDescBtn.setOnClickListener(v -> requestTextPermission());
    }

    /**
     * Request appropriate image permission based on Android version.
     */
    private void requestImagePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Request appropriate video permission based on Android version.
     */
    private void requestVideoPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            videoPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            videoPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Request appropriate text file permission based on Android version.
     */
    private void requestTextPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            textPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openTextFilePicker();
        }
    }

    /**
     * Setup click listeners for action buttons (save).
     */
    private void setupActionButtons() {
        saveBtn.setOnClickListener(v -> handleSaveOperation());
    }

    /**
     * Handle save operation with comprehensive validation and processing.
     */
    private void handleSaveOperation() {
        // Prevent multiple save operations
        if (isSaving || toastShown) return;

        // Validate input fields and show appropriate error messages
        if (!isInputValid()) return;

        // Start save process
        isSaving = true;
        toastShown = false;

        // Clean up media resources before saving
        cleanupMediaResources();

        // Process save based on current mode
        if (MODE_EDIT.equals(mode) && currentLesson != null) {
            saveEditMode();
        } else if (MODE_ADD.equals(mode)) {
            saveAddMode();
        }
    }

    /**
     * Validate all input fields and show appropriate error messages.
     * @return true if all validation passes, false otherwise
     */
    private boolean isInputValid() {
        // Check for existing validation errors
        if (lessonNameEt.getError() != null || shortDescEt.getError() != null) {
            Toast.makeText(this, "מלא כראוי את השדה/ות השגוי/ים.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate all required fields
        return validateInputFields();
    }

    /**
     * Handle save operation for ADD mode - create new lesson.
     */
    private void saveAddMode() {
        String lessonName = lessonNameEt.getText().toString();

        // Save all files to internal storage
        String imagePath = saveFileToInternalStorage(selectedImageUri, "lesson_" + lessonName + "_image.jpg");
        String txtPath = saveFileToInternalStorage(fileUri, "lesson_" + lessonName + "_description.txt");
        String videoPath = saveFileToInternalStorage(videoUri, "lesson_" + lessonName + "_video.mp4");

        // Check if any file saving failed
        if (imagePath == null || videoPath == null || txtPath == null) {
            Toast.makeText(this, "נוצרה שגיאה בטעינת הקבצים, אנא נסו שנית.", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }

        // Create and save new lesson
        Lesson newLesson = createNewLesson(imagePath, txtPath, videoPath);
        lessonsViewModel.insert(newLesson);
        Toast.makeText(this, "השיעור נשמר בהצלחה", Toast.LENGTH_SHORT).show();
        finishSafely();
    }

    /**
     * Create new lesson object with provided file paths.
     * @param imagePath Path to saved image file
     * @param txtPath Path to saved text file
     * @param videoPath Path to saved video file
     * @return New Lesson object with all data
     */
    private Lesson createNewLesson(String imagePath, String txtPath, String videoPath) {
        return new Lesson(
                lessonNameEt.getText().toString(),
                imagePath,
                shortDescEt.getText().toString(),
                lessonGuideEt.getText().toString(),
                levelSpn.getSelectedItem().toString(),
                videoPath,
                txtPath
        );
    }

    /**
     * Load user guide name from SharedPreferences and set it to the guide field.
     */
    private void loadUserGuide() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        long userId = prefs.getLong(KEY_USER_ID, -1);
        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                lessonGuideEt.setText(user.getUsername());
            }
        });
    }

    /**
     * Open image picker using document picker intent.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Open video picker using document picker intent with persistent permissions.
     */
    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        videoPickerLauncher.launch(intent);
    }

    /**
     * Open text file picker using document picker intent.
     */
    private void openTextFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        textFilePickerLauncher.launch(intent);
    }

    /**
     * Read text content from URI and display it in the text view.
     * @param uri URI of the text file to read
     */
    private void readTextFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            String content = stringBuilder.toString();
            lessonLongDescTx.setText(content);

            // Show text area if content exists
            if (!content.trim().isEmpty()) {
                findViewById(R.id.text_present).setVisibility(android.view.View.VISIBLE);
            }

        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בטעינת הקובץ", Toast.LENGTH_SHORT).show();
            findViewById(R.id.text_present).setVisibility(android.view.View.GONE);
        }
    }

    /**
     * Load lesson data into UI components for editing mode.
     * @param lesson The lesson object to load data from
     */
    private void loadLessonData(Lesson lesson) {
        // Reset user selection flags when loading lesson data
        resetUserSelectionFlags();

        // Save original file paths
        saveOriginalFilePaths(lesson);

        // Check if this is a resource file
        checkIfResourceFile();

        // Load basic lesson information
        loadBasicLessonInfo(lesson);

        // Load media files
        loadLessonMedia(lesson);

        // Set level spinner selection
        setLevelSpinnerSelection(lesson);
    }

    /**
     * Reset all user selection flags when loading existing lesson data.
     */
    private void resetUserSelectionFlags() {
        userSelectedNewImage = false;
        userSelectedNewVideo = false;
        userSelectedNewText = false;
    }

    /**
     * Save original file paths from lesson data.
     * @param lesson The lesson containing original file paths
     */
    private void saveOriginalFilePaths(Lesson lesson) {
        originalImagePath = lesson.getLessonPhoto();
        originalVideoPath = lesson.getLessonVideo();
        originalTextPath = lesson.getLongDescription();
    }

    /**
     * Check if any of the lesson files are resource files.
     */
    private void checkIfResourceFile() {
        isResourceFile = (originalImagePath != null && originalImagePath.startsWith("android.resource://")) ||
                (originalVideoPath != null && originalVideoPath.startsWith("android.resource://")) ||
                (originalTextPath != null && originalTextPath.startsWith("android.resource://"));
    }

    /**
     * Load basic lesson information into UI fields.
     * @param lesson The lesson containing basic information
     */
    private void loadBasicLessonInfo(Lesson lesson) {
        lessonNameEt.setText(lesson.getLessonName());
        shortDescEt.setText(lesson.getShortDescription());
        lessonGuideEt.setText(lesson.getLessonGuide());
    }

    /**
     * Load all media files (image, video, text) for the lesson.
     * @param lesson The lesson containing media file paths
     */
    private void loadLessonMedia(Lesson lesson) {
        loadLessonImage();
        loadLessonVideo();
        loadLessonText();
    }

    /**
     * Load lesson image from original path or resource.
     */
    private void loadLessonImage() {
        if (originalImagePath != null && !originalImagePath.isEmpty()) {
            try {
                if (originalImagePath.startsWith("android.resource://")) {
                    selectedImageUri = Uri.parse(originalImagePath);
                } else {
                    selectedImageUri = getFileProviderUri(originalImagePath);
                }
                addPhotoBtn.setImageURI(selectedImageUri);
            } catch (Exception e) {
                selectedImageUri = Uri.fromFile(new File(originalImagePath));
                addPhotoBtn.setImageURI(selectedImageUri);
            }
        }
    }

    /**
     * Get FileProvider URI for internal storage files.
     * @param filePath The file path to convert to URI
     * @return URI using FileProvider
     */
    private Uri getFileProviderUri(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", file);
        }
        return Uri.fromFile(file);
    }

    /**
     * Load lesson video from original path or resource.
     */
    private void loadLessonVideo() {
        if (originalVideoPath != null && !originalVideoPath.isEmpty() && !isSaving) {
            try {
                if (originalVideoPath.startsWith("android.resource://")) {
                    videoUri = Uri.parse(originalVideoPath);
                    setupVideoPlayer(videoUri);
                } else {
                    File videoFile = new File(originalVideoPath);
                    if (videoFile.exists()) {
                        videoUri = Uri.fromFile(videoFile);
                        setupVideoPlayer(videoUri);
                    } else {
                        handleVideoLoadError();
                    }
                }
            } catch (Exception e) {
                if (!isSaving) {
                    Toast.makeText(this, "שגיאה בטעינת הווידאו", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Handle video loading error by showing message and hiding video area.
     */
    private void handleVideoLoadError() {
        Toast.makeText(this, "קובץ הווידאו לא נמצא", Toast.LENGTH_SHORT).show();
        findViewById(R.id.video_present).setVisibility(android.view.View.GONE);
    }

    /**
     * Load lesson text content from original path or resource.
     */
    private void loadLessonText() {
        if (originalTextPath != null && !originalTextPath.isEmpty()) {
            try {
                if (originalTextPath.startsWith("android.resource://")) {
                    fileUri = Uri.parse(originalTextPath);
                    readTextFromUri(fileUri);
                } else {
                    loadTextFromFile();
                }
            } catch (Exception e) {
                handleTextLoadError();
            }
        }
    }

    /**
     * Load text content from internal storage file.
     */
    private void loadTextFromFile() {
        File textFile = new File(originalTextPath);
        if (textFile.exists()) {
            fileUri = Uri.fromFile(textFile);
            readTextFromUri(fileUri);
            findViewById(R.id.text_present).setVisibility(android.view.View.VISIBLE);
        } else {
            handleTextLoadError();
            Toast.makeText(this, "קובץ התיאור הארוך לא נמצא", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle text loading error by clearing content and hiding text area.
     */
    private void handleTextLoadError() {
        lessonLongDescTx.setText("");
        findViewById(R.id.text_present).setVisibility(android.view.View.GONE);
        Toast.makeText(this, "שגיאה בטעינת התיאור הארוך", Toast.LENGTH_SHORT).show();
    }

    /**
     * Set level spinner selection based on lesson data.
     * @param lesson The lesson containing level information
     */
    private void setLevelSpinnerSelection(Lesson lesson) {
        String[] levels = getResources().getStringArray(R.array.levels);
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equalsIgnoreCase(lesson.getLevel())) {
                levelSpn.setSelection(i);
                break;
            }
        }
    }

    /**
     * Save file from URI to internal storage for permanent access.
     * @param uri URI of the file to save
     * @param fileName Name for the saved file
     * @return The absolute path of the saved file, or null if failed
     */
    private String saveFileToInternalStorage(Uri uri, String fileName) {
        if (uri == null) return null;

        try {
            // Open input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create file in internal storage
            File file = new File(getFilesDir(), fileName);

            // Copy data from input to output stream
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            inputStream.close();
            return file.getAbsolutePath();

        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בשמירת הקובץ: " + fileName, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Handle save operation for EDIT mode - update existing lesson.
     */
    private void saveEditMode() {
        // Remove observer to prevent conflicts during save
        lessonsViewModel.getLessonById(lessonId).removeObservers(this);

        // Generate file names for this lesson
        String[] fileNames = generateFileNames();

        // Determine which files need to be saved
        String[] filePaths = determineFilePaths(fileNames);

        // Validate that all required files are available
        if (!areAllFilePathsValid(filePaths)) return;

        // Update lesson with new data
        updateCurrentLessonData(filePaths);

        // Save to database with delay to prevent UI conflicts
        saveToDatabase();
    }

    /**
     * Generate standardized file names for lesson files.
     * @return Array containing [imageFileName, txtFileName, videoFileName]
     */
    private String[] generateFileNames() {
        String imageFileName = "lesson_" + lessonId + "_image.jpg";
        String txtFileName = "lesson_" + lessonId + "_description.txt";
        String videoFileName = "lesson_" + lessonId + "_video.mp4";
        return new String[]{imageFileName, txtFileName, videoFileName};
    }

    /**
     * Determine file paths based on user selections and original paths.
     * @param fileNames Array of generated file names
     * @return Array containing [imagePath, txtPath, videoPath]
     */
    private String[] determineFilePaths(String[] fileNames) {
        String imagePath = determineImagePath(fileNames[0]);
        String txtPath = determineTextPath(fileNames[1]);
        String videoPath = determineVideoPath(fileNames[2]);
        return new String[]{imagePath, txtPath, videoPath};
    }

    /**
     * Determine image file path - save new file if user selected one, otherwise use original.
     * @param imageFileName Generated image file name
     * @return Path to image file or null if save failed
     */
    private String determineImagePath(String imageFileName) {
        if (userSelectedNewImage && selectedImageUri != null) {
            String imagePath = saveFileToInternalStorage(selectedImageUri, imageFileName);
            if (imagePath == null) {
                Toast.makeText(this, "נוצרה שגיאה בשמירת התמונה", Toast.LENGTH_SHORT).show();
                isSaving = false;
            }
            return imagePath;
        }
        return originalImagePath;
    }

    /**
     * Determine text file path - save new file if user selected one, otherwise use original.
     * @param txtFileName Generated text file name
     * @return Path to text file or null if save failed
     */
    private String determineTextPath(String txtFileName) {
        if (userSelectedNewText && fileUri != null && !lessonLongDescTx.getText().toString().trim().isEmpty()) {
            String txtPath = saveFileToInternalStorage(fileUri, txtFileName);
            if (txtPath == null) {
                Toast.makeText(this, "נוצרה שגיאה בשמירת קובץ הטקסט", Toast.LENGTH_SHORT).show();
                isSaving = false;
            }
            return txtPath;
        }
        return originalTextPath;
    }

    /**
     * Determine video file path - save new file if user selected one, otherwise use original.
     * @param videoFileName Generated video file name
     * @return Path to video file or null if save failed
     */
    private String determineVideoPath(String videoFileName) {
        if (userSelectedNewVideo && videoUri != null) {
            String videoPath = saveFileToInternalStorage(videoUri, videoFileName);
            if (videoPath == null) {
                Toast.makeText(this, "נוצרה שגיאה בשמירת הווידאו", Toast.LENGTH_SHORT).show();
                isSaving = false;
            }
            return videoPath;
        }
        return originalVideoPath;
    }

    /**
     * Validate that all file paths are not null.
     * @param filePaths Array of file paths to validate
     * @return true if all paths are valid, false otherwise
     */
    private boolean areAllFilePathsValid(String[] filePaths) {
        if (filePaths[0] == null || filePaths[1] == null || filePaths[2] == null) {
            Toast.makeText(this, "נוצרה שגיאה - חסרים קבצים נדרשים", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return false;
        }
        return true;
    }

    /**
     * Update current lesson object with new data from UI and file paths.
     * @param filePaths Array containing [imagePath, txtPath, videoPath]
     */
    private void updateCurrentLessonData(String[] filePaths) {
        currentLesson.setLessonName(lessonNameEt.getText().toString());
        currentLesson.setShortDescription(shortDescEt.getText().toString());
        currentLesson.setLessonGuide(lessonGuideEt.getText().toString());
        currentLesson.setLevel(levelSpn.getSelectedItem().toString());
        currentLesson.setLessonPhoto(filePaths[0]); // imagePath
        currentLesson.setLongDescription(filePaths[1]); // txtPath
        currentLesson.setLessonVideo(filePaths[2]); // videoPath
    }

    /**
     * Save updated lesson to database with delay to prevent UI conflicts.
     */
    private void saveToDatabase() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!toastShown && !isFinishing()) {
                lessonsViewModel.update(currentLesson);
                toastShown = true;
                Toast.makeText(this, "השיעור עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                finishSafely();
            }
        }, SAVE_DELAY_MS);
    }

    /**
     * Validate all required input fields before saving.
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateInputFields() {
        return validateLessonName() &&
                validateShortDescription() &&
                validateLessonGuide() &&
                validateVideo() &&
                validateImage() &&
                validateLongDescription();
    }

    /**
     * Validate lesson name field.
     * @return true if valid, false otherwise
     */
    private boolean validateLessonName() {
        if (lessonNameEt.getText().toString().isEmpty()) {
            Toast.makeText(this, "נא להזין שם שיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validate short description field.
     * @return true if valid, false otherwise
     */
    private boolean validateShortDescription() {
        if (shortDescEt.getText().toString().isEmpty()) {
            Toast.makeText(this, "נא להזין תיאור קצר לשיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validate lesson guide field.
     * @return true if valid, false otherwise
     */
    private boolean validateLessonGuide() {
        if (lessonGuideEt.getText().toString().isEmpty()) {
            Toast.makeText(this, "נא להזין שם מדריך/ה לשיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validate video selection.
     * @return true if valid, false otherwise
     */
    private boolean validateVideo() {
        if (videoUri == null) {
            Toast.makeText(this, "נא להוסיף סרטון לשיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validate image selection.
     * @return true if valid, false otherwise
     */
    private boolean validateImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "נא להוסיף תמונה לשיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validate long description text.
     * @return true if valid, false otherwise
     */
    private boolean validateLongDescription() {
        if (lessonLongDescTx.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "נא להוסיף תיאור ארוך לשיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Setup video player with proper error handling and listeners.
     * @param uri URI of the video to play
     */
    private void setupVideoPlayer(Uri uri) {
        if (uri == null || isFinishing() || isDestroyed()) return;

        try {
            // Validate URI accessibility
            validateVideoUri(uri);

            // Setup video player components
            setupVideoPlayerComponents();

            // Configure video player listeners
            configureVideoPlayerListeners();

            // Start video loading
            lessonVideo.setVideoURI(uri);

        } catch (Exception e) {
            handleVideoPlayerError();
        }
    }

    /**
     * Validate that video URI is accessible.
     * @param uri Video URI to validate
     * @throws Exception if URI cannot be accessed
     */
    private void validateVideoUri(Uri uri) throws Exception {
        getContentResolver().openInputStream(uri).close();
    }

    /**
     * Setup video player UI components (MediaController).
     */
    private void setupVideoPlayerComponents() {
        // Clear existing listeners to prevent memory leaks
        lessonVideo.setOnPreparedListener(null);
        lessonVideo.setOnErrorListener(null);
        lessonVideo.setOnCompletionListener(null);

        // Setup new MediaController
        if (mediaController != null) {
            mediaController.hide();
        }
        mediaController = new MediaController(this);
        mediaController.setAnchorView(lessonVideo);
        lessonVideo.setMediaController(mediaController);
    }

    /**
     * Configure video player event listeners.
     */
    private void configureVideoPlayerListeners() {
        // Set up video prepared listener
        lessonVideo.setOnPreparedListener(mp -> {
            if (!isFinishing() && !isDestroyed() && !isSaving) {
                try {
                    mp.setLooping(true);
                    findViewById(R.id.video_present).setVisibility(android.view.View.VISIBLE);
                    lessonVideo.start();
                } catch (Exception e) {
                    Toast.makeText(this, "שגיאה בהפעלת הווידאו", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up error listener for video playback issues
        lessonVideo.setOnErrorListener((mp, what, extra) -> {
            if (!isFinishing() && !isDestroyed() && !isSaving) {
                Toast.makeText(this, "שגיאה בנגינת הווידאו - קובץ לא נתמך או פגום", Toast.LENGTH_SHORT).show();
                findViewById(R.id.video_present).setVisibility(android.view.View.GONE);
            }
            return true;
        });
    }

    /**
     * Handle video player setup errors.
     */
    private void handleVideoPlayerError() {
        if (!isFinishing() && !isDestroyed() && !isSaving) {
            Toast.makeText(this, "שגיאה בגישה לקובץ הווידאו", Toast.LENGTH_SHORT).show();
        }
        videoUri = null;
        findViewById(R.id.video_present).setVisibility(android.view.View.GONE);
    }

    /**
     * Clean up media resources to prevent memory leaks and window leaks.
     */
    private void cleanupMediaResources() {
        try {
            cleanupVideoPlayer();
            cleanupMediaController();
            hideVideoArea();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleanup video player resources and listeners.
     */
    private void cleanupVideoPlayer() {
        if (lessonVideo != null) {
            // Remove all listeners
            lessonVideo.setOnPreparedListener(null);
            lessonVideo.setOnErrorListener(null);
            lessonVideo.setOnCompletionListener(null);

            // Stop playback if playing
            if (lessonVideo.isPlaying()) {
                lessonVideo.stopPlayback();
            }
            lessonVideo.setVideoURI(null);
            lessonVideo.setMediaController(null);
        }
    }

    /**
     * Cleanup media controller resources.
     */
    private void cleanupMediaController() {
        if (mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
    }

    /**
     * Hide video display area.
     */
    private void hideVideoArea() {
        findViewById(R.id.video_present).setVisibility(android.view.View.GONE);
    }

    /**
     * Finish activity safely with proper cleanup and result data.
     */
    private void finishSafely() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_UPDATED, true);
        setResult(RESULT_OK, resultIntent);

        // Delay finish to prevent UI conflicts
        lessonVideo.postDelayed(() -> {
            if (!isFinishing()) {
                finish();
            }
        }, SAVE_DELAY_MS);
    }

    /**
     * Called when activity is paused - hide media controller and pause video.
     * Saves battery and system resources when activity is not visible.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Hide media controller when activity is not visible
        if (mediaController != null) {
            mediaController.hide();
        }

        // Pause video playback to save battery and resources
        if (lessonVideo != null && lessonVideo.isPlaying()) {
            lessonVideo.pause();
        }
    }

    /**
     * Called when activity is destroyed - cleanup resources and reset flags.
     * Prevents memory leaks and ensures proper resource cleanup.
     */
    @Override
    protected void onDestroy() {
        // Reset state flags
        isSaving = false;
        toastShown = false;

        // Clean up all media resources
        cleanupMediaResources();

        super.onDestroy();
    }
}