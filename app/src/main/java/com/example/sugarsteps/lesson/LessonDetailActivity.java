package com.example.sugarsteps.lesson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sugarsteps.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LessonDetailActivity extends AppCompatActivity {

    private ImageButton backBtn; // Back button to lessonlist
    private CheckBox doneChkBox; // Lesson has done
    private ImageButton likeBtn; // Like button for later
    private TextView nameLessonTv; // Lesson name
    private TextView nameGuideTv; // Guide name
    private VideoView lessonVideo; // Lesson Video
    private TextView descriptionTv, markAsDoneTv; // Description text
    private boolean like = false; // Flag for like lesson
    private ExecutorService executor = Executors.newSingleThreadExecutor(); // For background1 tasks
    private MediaController mediaController; // Adding to destroy it - so it won't leaked
    private boolean isActivityDestroyed = false; // Checking if Activity destroyed
    private View rootLayout;     // reference to root layout view to set background dynamically
    private int selectedBackground = 1; // default background ID


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lesson_view);

        int lessonId = getLessonIdFromIntent();
        if (lessonId == -1) { // Lesson ID val
            handleLessonNotFound();
            return; // Stopping this activity
        }

        initViews();                      // Initialize UI components by findViewById
        loadBackgroundFromPreferences(); // Load and apply background from saved preferences
        setupLikeButton();               // Setup like button click behavior
        setupDoneCheckbox();             // Setup done checkbox click behavior
        loadLessonData(lessonId);        // Loading lesson info according to lessonId
        setupBackButton(lessonId);       // Setup back button to save changes and finish activity
    }
    /**
     * Load the selected background from SharedPreferences and apply it.
     */
    private void loadBackgroundFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("SugarStepsPref", MODE_PRIVATE);
        // Get saved background ID or default to 1
        selectedBackground = prefs.getInt("selected_background", 1);
        applyBackground(selectedBackground); // Apply background to root layout
    }

    /**
     * Apply the selected background resource to the root layout.
     * @param backgroundId The selected background ID from preferences.
     */
    private void applyBackground(int backgroundId) {
        if (rootLayout == null) return; // Prevent NPE if view not initialized

        int drawableResId;
        switch (backgroundId) {
            case 2:
                drawableResId = R.drawable.background2; // Background 2 drawable resource
                break;

            default:
                drawableResId = R.drawable.background1; // Default background drawable resource
                break;
        }

        rootLayout.setBackgroundResource(drawableResId); // Set background drawable
    }

    /**
     * Retrieve lesson ID from the incoming intent.
     * @return lessonId if exists, otherwise -1.
     */
    private int getLessonIdFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("lessonId")) {
            return intent.getIntExtra("lessonId", -1);
        }
        return -1; // Return -1 if no valid lesson ID
    }

    /**
     * Handle the case where the lesson ID is invalid.
     * Shows "page not found" layout and redirects to main list.
     */
    private void handleLessonNotFound() {
        setContentView(R.layout.page_not_found);       // Loading layout of page not found
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, LessonsListActivity.class);
            startActivity(intent);                      // Navigate to lessons list
            finish();                                  // Close current activity
        }, 3000);                           // after 3 sec we go back to main page
    }


    /**
     * Initialize all views from the layout.
     */
    private void initViews() {
        backBtn = findViewById(R.id.btn_back);
        doneChkBox = findViewById(R.id.btn_lesson_check);
        likeBtn = findViewById(R.id.imgbtn_like_lesson);
        nameLessonTv = findViewById(R.id.tv_lesson_header);
        nameGuideTv = findViewById(R.id.tv_lesson_guide);
        lessonVideo = findViewById(R.id.video_lesson);
        descriptionTv = findViewById(R.id.tv_lesson_recipe);
        markAsDoneTv = findViewById(R.id.tx_done);
        rootLayout = findViewById(R.id.rootLayout);
    }

    /**
     * Set up the like button with animation and toggle functionality.
     */
    private void setupLikeButton() {
        likeBtn.setOnClickListener(v -> {
            // Animate the heart (like button) on click
            likeBtn.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        like = !like;                         // Toggle the 'like' boolean flag
                        likeBtn.setImageResource(like ? R.drawable.ic_heart_full : R.drawable.ic_heart_border);         // Change the heart icon depending on 'like' state
                        likeBtn.animate().scaleX(1f).scaleY(1f).setDuration(150).start();                    // Animate scaling back to full size (appear effect)

                    })
                    .start();
        });
    }

    /**
     * Set up the "mark as done" checkbox with animation and toast messages.
     */
    private void setupDoneCheckbox() {
        doneChkBox.setOnClickListener(v -> {
            // Animate the checkbox on click
            doneChkBox.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        // Animate scaling back to normal size
                        doneChkBox.animate().scaleX(1.25f).scaleY(1.25f).setDuration(150).start();
                        updateMarkText(doneChkBox.isChecked());                // Update the tx according to whether the checkbox is checked or not
                        if (doneChkBox.isChecked()) {
                            Toast.makeText(this, "השיעור סומן כבוצע", Toast.LENGTH_SHORT).show();             // Make Toast that the lesson is done
                        } else {
                            Toast.makeText(this, "הסימון של השיעור בוטל", Toast.LENGTH_SHORT).show();         // Make Toast that the lesson is un-done
                        }
                    })
                    .start();
        });
    }

    /**
     * Load lesson data into the UI.
     * @param lessonId ID of the lesson to load.
     */
    private void loadLessonData(int lessonId) {
        LessonsViewModel lessonsViewModel = new ViewModelProvider(this).get(LessonsViewModel.class);
        lessonsViewModel.getLessonById(lessonId).observe(this, lessons -> {
            if (lessons == null) return;

            nameLessonTv.setText(lessons.getLessonName());
            nameGuideTv.setText(lessons.getLessonGuide());
            loadLongDescription(lessons.getLongDescription()); // Load description text asynchronously

            updateMarkText(lessons.isCheck()); // Update done label text
            doneChkBox.setChecked(lessons.isCheck()); // Set checkbox state
            like = lessons.isLiked();           // Set like flag
            likeBtn.setImageResource(like ? R.drawable.ic_heart_full : R.drawable.ic_heart_border); // Update icon

            setupVideo(lessons.getLessonVideo()); // Setup video playback
        });
    }

    /**
     * Set up the back button to update lesson data if changed.
     * @param lessonId The current lesson ID.
     */
    private void setupBackButton(int lessonId) {
        backBtn.setOnClickListener(v -> {
            LessonsViewModel lessonsViewModel = new ViewModelProvider(this).get(LessonsViewModel.class);
            lessonsViewModel.getLessonById(lessonId).observe(this, lessons -> {
                boolean needsUpdate = false;

                // Check if liked state changed
                if (lessons.isLiked() != like) {
                    needsUpdate = true;
                    lessons.setLiked(like);
                }

                // Check if done state changed
                if (doneChkBox.isChecked() != lessons.isCheck()) {
                    needsUpdate = true;
                    lessons.setCheck(doneChkBox.isChecked());
                }

                if (needsUpdate) {
                    lessonsViewModel.update(lessons); // Save changes in ViewModel
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updated", true); // Notify caller changes happened
                    setResult(RESULT_OK, resultIntent);
                } else {
                    setResult(RESULT_CANCELED); // No changes made
                }
                finish(); // Close activity
            });
        });
    }


    /**
     * Load long description from a file path or resource.
     * @param descPath Path to the description file.
     */
    private void loadLongDescription(String descPath) {
        if (descPath == null || descPath.isEmpty()) {
            descriptionTv.setText("אין תיאור זמין");
            return;
        }

        // Async - do in background1
        executor.execute(() -> {
            try {
                InputStream inputStream;

                if (descPath.startsWith("android.resource://")) {
                    // File from Resource
                    Uri uri = Uri.parse(descPath);
                    inputStream = getContentResolver().openInputStream(uri);
                } else {
                    // File from intenal Storage
                    java.io.File file = new java.io.File(descPath);

                    if (!file.exists()) {
                        runOnUiThread(() -> {
                            if (!isActivityDestroyed) {
                                descriptionTv.setText("קובץ התיאור לא נמצא");
                            }
                        });
                        return;
                    }

                    inputStream = new java.io.FileInputStream(file);
                }

                // Reading thr content
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                reader.close();
                inputStream.close();

                // Update UI with description text on main thread
                if (!isActivityDestroyed) {
                    runOnUiThread(() -> {
                        if (!isActivityDestroyed) {
                            descriptionTv.setText(stringBuilder.toString());
                        }
                    });
                }

            } catch (Exception e) {
                if (!isActivityDestroyed) {
                    runOnUiThread(() -> {
                        if (!isActivityDestroyed) {
                            descriptionTv.setText("שגיאה בטעינת התיאור");
                        }
                    });
                }
            }
        });
    }


    /**
     * Setup video from a given file path.
     * @param videoPath Path to the video file.
     */
    private void setupVideo(String videoPath) {
        if (isActivityDestroyed || videoPath == null || videoPath.isEmpty()) {
            return;
        }

        try {
            Uri videoUri;

            if (videoPath.startsWith("android.resource://")) {
                // File from raw folder
                videoUri = Uri.parse(videoPath);
            } else {
                // File from internal storage
                java.io.File videoFile = new java.io.File(videoPath);

                if (!videoFile.exists()) {
                    Toast.makeText(this, "קובץ הווידאו לא נמצא", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check file size
                long fileSize = videoFile.length();

                if (fileSize == 0) {
                    Toast.makeText(this, "קובץ הווידאו ריק", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (fileSize < 1000) { // Less than 1KB
                    Toast.makeText(this, "קובץ הווידאו קטן מדי או פגום", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Just use the file directly - no copying
                videoUri = Uri.fromFile(videoFile);
            }

            // Clean previous video setup
            cleanupVideo();

            // Setup new video with delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                    setupVideoPlayer(videoUri);
                }
            }, 300); // Longer delay

        } catch (Exception e) {
            if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                Toast.makeText(this, "שגיאה בטעינת הווידאו: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Setup the VideoView with given URI and listeners.
     * @param videoUri The URI of the video file.
     */
    private void setupVideoPlayer(Uri videoUri) {
        if (isActivityDestroyed || videoUri == null) {
            return;
        }

        try {
            // Clean previous setup first
            if (lessonVideo != null) {
                lessonVideo.setOnPreparedListener(null);
                lessonVideo.setOnErrorListener(null);
                lessonVideo.setOnCompletionListener(null);
                if (lessonVideo.isPlaying()) {
                    lessonVideo.stopPlayback();
                }
                lessonVideo.setVideoURI(null);
            }

            // Wait a bit before setting up new video
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                    try {
                        // Create new MediaController
                        mediaController = new MediaController(this);
                        mediaController.setAnchorView(lessonVideo);
                        lessonVideo.setMediaController(mediaController);

                        // Set up error listener FIRST
                        lessonVideo.setOnErrorListener((mp, what, extra) -> {
                            if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                                String errorMsg = "שגיאה בהפעלת הווידאו";
                                if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                                    errorMsg += " - קובץ לא נתמך";
                                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                                    errorMsg += " - בעיית שרת";
                                }
                                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                            return true;
                        });

                        // Set up prepared listener
                        lessonVideo.setOnPreparedListener(mp -> {
                            if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                                try {
                                    mp.setLooping(true);
                                    lessonVideo.start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // Finally, set the video URI
                        lessonVideo.setVideoURI(videoUri);

                    } catch (Exception e) {
                        if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "שגיאה בהגדרת נגן הווידאו: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }, 200);

        } catch (Exception e) {
            e.printStackTrace();
            if (!isActivityDestroyed && !isFinishing() && !isDestroyed()) {
                Toast.makeText(this, "שגיאה כללית בנגן הווידאו: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Cleanup video player resources.
     */
    private void cleanupVideo() {
        try {
            if (lessonVideo != null) {
                lessonVideo.setOnPreparedListener(null);
                lessonVideo.setOnErrorListener(null);
                lessonVideo.setOnCompletionListener(null);

                if (lessonVideo.isPlaying()) {
                    lessonVideo.stopPlayback();
                }
                lessonVideo.setVideoURI(null);
                lessonVideo.setMediaController(null);
            }

            if (mediaController != null) {
                mediaController.hide();
                mediaController = null;
            }
        } catch (Exception e) {
        }
    }


    /**
     * Update the "Mark as done" label according to checkbox state.
     * @param isChecked true if done, false otherwise.
     */
    private void updateMarkText(boolean isChecked) {
        markAsDoneTv.setText(isChecked ? R.string.lesson_done : R.string.lesson_check);
    }

    // On pause to Activity
    @Override
    protected void onPause() {
        super.onPause();

        // Pause video if playing
        if (lessonVideo != null && lessonVideo.isPlaying()) {
            lessonVideo.pause();
        }

        // Hide media controller
        if (mediaController != null) {
            mediaController.hide();
        }
    }

    // When the Activity is destroyed
    @Override
    protected void onDestroy() {
        isActivityDestroyed = true;

        // Clean up video resources
        cleanupVideo();

        // Clean up executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        super.onDestroy();
    }

}
