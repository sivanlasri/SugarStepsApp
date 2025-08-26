package com.example.sugarsteps.user;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sugarsteps.lesson.Lesson;

import java.util.List;

/**
 * Data Access Object (DAO) interface for User entity operations.
 * Defines database operations for managing users in the SugarSteps application.
 *
 * This interface uses Room annotations to generate the implementation automatically.
 * All operations are performed on a background thread except where noted.
 *
 * Features:
 * - Complete CRUD operations for User entities
 * - LiveData integration for reactive UI updates
 * - Batch operations for multiple entities
 * - Custom query methods for specific use cases
 *
 * @author Sivan Lasri
 * @version 5.0
 */
@Dao
public interface UserDao {

    /**
     * Insert a new user into the database.
     *
     * This method performs a synchronous insert operation and returns the auto-generated
     * primary key of the inserted user. The operation runs on a background thread when
     * called through Repository pattern.
     *
     * @param user The User object to insert into the database.
     *             Must not be null and should have all required fields populated.
     * @return The auto-generated primary key (userId) of the newly inserted user.
     *         Returns the actual database ID that can be used for future operations.
     *
     * @throws android.database.sqlite.SQLiteConstraintException if unique constraints are violated
     * @throws IllegalArgumentException if user parameter is null
     *
     */
    @Insert
    long insertUser(User user);

    /**
     * Insert multiple lessons into the database in a single transaction.
     *
     * This is a batch operation that inserts all lessons in the provided list.
     * The operation is atomic - either all lessons are inserted successfully,
     * or none are inserted if an error occurs.
     *
     *
     * @param lessonsList List of Lesson objects to insert. Must not be null.
     *                   Individual lessons in the list should have all required fields populated.
     *
     * @throws android.database.sqlite.SQLiteConstraintException if any lesson violates constraints
     * @throws IllegalArgumentException if lessonsList parameter is null
     *
     */
    @Insert
    void insertAll(List<Lesson> lessonsList);

    /**
     * Update an existing user in the database.
     *
     * Updates the user record based on the primary key (userId).
     * All fields of the user object will be updated in the database.
     * If no user with the given ID exists, no operation is performed.
     *
     * @param user The User object containing updated information.
     *             Must not be null and must have a valid userId (primary key).
     *
     * @throws IllegalArgumentException if user parameter is null
     * @throws android.database.sqlite.SQLiteConstraintException if update violates constraints
     *
     */
    @Update
    void updateUser(User user);

    /**
     * Delete a specific user from the database.
     *
     * Removes the user record that matches the primary key of the provided user object.
     * If no matching user is found, no operation is performed.
     *
     * @param user The User object to delete. Must not be null and should have a valid userId.
     *
     * @throws IllegalArgumentException if user parameter is null
     *
     */
    @Delete
    void deleteUser(User user);

    /**
     * Retrieve all users from the database as LiveData.
     *
     * Returns a LiveData object containing a list of all users in the database.
     * The LiveData will automatically update when users are added, modified, or deleted.
     * This enables reactive UI updates without manual refresh calls.
     *
     * The returned LiveData should be observed on the main thread.
     * The actual database query runs on a background thread automatically.
     *
     * @return LiveData containing a List of all User objects in the database.
     *         The list may be empty if no users exist, but will never be null.
     *         Updates automatically when database changes occur.
     *
     */
    @Query("SELECT * FROM User")
    LiveData<List<User>> getAllUsers();

    /**
     * Retrieve a specific user by their unique identifier.
     *
     * Returns a LiveData object containing the user with the specified ID.
     * The LiveData will automatically update if the user's information changes.
     *
     * @param id The unique identifier (primary key) of the user to retrieve.
     *           Must be a valid positive long value.
     *
     * @return LiveData containing the User object with the specified ID.
     *         Will contain null if no user with the given ID exists.
     *         Updates automatically when the user's data changes.
     *
     */
    @Query("SELECT * FROM User WHERE userId = :id")
    LiveData<User> getUserById(long id);

    /**
     * Retrieve a user by their username (synchronous operation).
     *
     * Finds and returns the first user with the specified username.
     * This is a synchronous operation that returns the actual User object,
     * not wrapped in LiveData.
     *
     * The LIMIT 1 ensures only one user is returned even if multiple users
     * somehow have the same username (though this should be prevented by constraints).
     *
     * @param username The username to search for. Must not be null or empty.
     *                Case-sensitive exact match is performed.
     *
     * @return The User object with the matching username, or null if no match is found.
     *         This is a direct object reference, not LiveData.
     *
     * @throws IllegalArgumentException if username parameter is null
     *
     */
    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    /**
     * Delete all users from the database.
     *
     * Removes every user record from the User table.
     * This operation is irreversible and should be used with caution.
     * Commonly used for data cleanup, testing, or reset functionality.
     *
     */
    @Query("DELETE FROM User")
    void deleteAllUsers();
}