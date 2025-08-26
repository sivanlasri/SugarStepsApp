package com.example.sugarsteps.lesson;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) interface for performing database operations
 * on the {@link Lesson} entity.

 * This interface defines CRUD operations (Create, Read, Update, Delete)
 * and is used by Room to generate the implementation at compile time.

 * All methods in this interface run their queries on a background thread
 * when used with Room, ensuring the UI thread is not blocked.
 *
 * Features:
 * - Insert a new lesson and retrieve its generated ID.
 * - Update existing lesson records.
 * - Delete a specific lesson.
 * - Retrieve all lessons (as observable LiveData).
 * - Retrieve a single lesson by its ID (as observable LiveData).
 *
 *
 * @author Sivan Lasri
 * @version 5.0
 */
@Dao
public interface LessonsDao {

    /**
     * Inserts a new lesson into the database.
     *
     * @param lesson the {@link Lesson} to insert
     * @return the generated ID of the inserted lesson
     */
    @Insert
    long insertLesson(Lesson lesson);

    /**
     * Updates an existing lesson in the database.
     *
     * @param lesson the {@link Lesson} object with updated values
     */
    @Update
    void updateLesson(Lesson lesson);

    /**
     * Deletes a lesson from the database.
     *
     * @param lesson the {@link Lesson} to delete
     */
    @Delete
    void deleteLesson(Lesson lesson);

    /**
     * Retrieves all lessons from the database.
     * Returned as a {@link LiveData} list so that the UI can observe
     * and automatically update when the underlying data changes.
     *
     * @return a {@link LiveData} list of all lessons
     */
    @Query("SELECT * FROM Lesson")
    LiveData<List<Lesson>> getAllLessons();

    /**
     * Retrieves a single lesson by its unique ID.
     * Returned as {@link LiveData} so that the UI can observe
     * changes to this lesson record.
     *
     * @param id the unique lesson ID
     * @return a {@link LiveData} object containing the lesson
     */
    @Query("SELECT * FROM Lesson WHERE lessonId = :id")
    LiveData<Lesson> getLessonById(long id);

}
