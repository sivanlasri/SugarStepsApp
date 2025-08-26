package com.example.sugarsteps.lesson;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * ViewModel class that provides data to the UI and survives configuration changes.
 *
 * Acts as a communication center between the {@link LessonsRepository}
 * and the UI (Activity/Fragment) in the MVVM architecture.
 *
 * @author
 *     Sivan Lasri
 * @version
 *     5.0
 */
public class LessonsViewModel extends AndroidViewModel {

    /** Reference to the repository that handles Lesson data operations. */
    private LessonsRepository lessonsRepository;

    /** LiveData list of all lessons, observed by the UI for automatic updates. */
    private LiveData<List<Lesson>> allLessons;

    /**
     * Constructs the ViewModel and initializes the repository and LiveData.
     *
     * @param application the application context, required by {@link AndroidViewModel}
     */
    public LessonsViewModel(Application application) {
        super(application);
        lessonsRepository = new LessonsRepository(application);
        allLessons = lessonsRepository.getAllLessons();
    }

    /**
     * Retrieves all lessons from the repository.
     *
     * Since this returns {@link LiveData}, the UI will automatically update
     * when the lessons in the database change.
     *
     * @return a {@link LiveData} list of all lessons
     */
    public LiveData<List<Lesson>> getAllLessons() {
        return allLessons;
    }

    /**
     * Inserts a new lesson into the database via the repository.
     *
     * @param lesson the {@link Lesson} object to insert
     */
    public void insert(Lesson lesson) {
        lessonsRepository.insert(lesson);
    }

    /**
     * Deletes a specific lesson from the database via the repository.
     *
     * @param lesson the {@link Lesson} object to delete
     */
    public void deleteLesson(Lesson lesson) {
        lessonsRepository.delete(lesson);
    }

    /**
     * Retrieves a lesson by its unique ID from the repository.
     *
     * Since this returns {@link LiveData}, the UI can observe it
     * and automatically update if the lesson changes.
     *
     * @param id the unique ID of the lesson
     * @return a {@link LiveData} object containing the lesson
     */
    public LiveData<Lesson> getLessonById(long id) {
        return lessonsRepository.getLessonById(id);
    }

    /**
     * Updates an existing lesson in the database via the repository.
     *
     * @param lesson the {@link Lesson} object to update
     */
    public void update(Lesson lesson) {
        lessonsRepository.update(lesson);
    }

}
