package com.example.sugarsteps.lesson;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sugarsteps.DB.SugarStepsDataBase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that provides a clean API for accessing {@link Lesson} data
 * from the underlying Room database through the {@link LessonsDao}.
 *
 * This class acts as a mediator between the data sources (Room database)
 * and the rest of the application, specifically the ViewModel and UI layers.
 *
 * Features:
 * - Retrieve all lessons as LiveData.
 * - Insert, update, and delete lessons asynchronously.
 * - Retrieve a lesson by its ID.
 *
 * Follows the Repository design pattern to promote separation of concerns
 * and easier testing/maintenance.
 *
 * @author Sivan Lasri
 * @version 8.0
 */
public class LessonsRepository {

    /** DAO for performing CRUD operations on Lesson entities. */
    private LessonsDao lessonsDao;

    /** LiveData list of all lessons, observed by the UI for automatic updates. */
    private LiveData<List<Lesson>> allLessons;

    /** Executor service for running database operations off the main thread. */
    private ExecutorService executorService;

    /**
     * Constructs the repository, initializing the database, DAO, LiveData list,
     * and the background executor service.
     *
     * @param application the application context used to get the Room database instance
     */
    public LessonsRepository(Application application) {
        // Get the singleton instance of the Room database
        SugarStepsDataBase db = SugarStepsDataBase.getDatabase(application);

        // Initialize the DAO
        lessonsDao = db.lessonsDao();

        // Retrieve all lessons as LiveData
        allLessons = lessonsDao.getAllLessons();

        // Executor for background tasks
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieves all lessons from the database.
     * <p>
     * Since this returns {@link LiveData}, the UI will automatically update
     * when the data changes in the database.
     *
     * @return a {@link LiveData} list of all lessons
     */
    public LiveData<List<Lesson>> getAllLessons() {
        return allLessons;
    }

    /**
     * Inserts a new lesson into the database asynchronously.
     *
     * @param lesson the {@link Lesson} object to insert
     */
    public void insert(final Lesson lesson) {
        executorService.execute(() -> lessonsDao.insertLesson(lesson));
    }

    /**
     * Deletes an existing lesson from the database asynchronously.
     *
     * @param lesson the {@link Lesson} object to delete
     */
    public void delete(final Lesson lesson) {
        executorService.execute(() -> lessonsDao.deleteLesson(lesson));
    }

    /**
     * Retrieves a specific lesson by its ID.
     *
     * Since this returns {@link LiveData}, the UI will automatically observe
     * and update when the lesson data changes.
     *
     * @param id the unique ID of the lesson
     * @return a {@link LiveData} object containing the lesson
     */
    public LiveData<Lesson> getLessonById(long id) {
        return lessonsDao.getLessonById(id);
    }

    /**
     * Updates an existing lesson in the database asynchronously.
     *
     * @param lesson the {@link Lesson} object to update
     */
    public void update(final Lesson lesson) {
        executorService.execute(() -> lessonsDao.updateLesson(lesson));
    }

}
