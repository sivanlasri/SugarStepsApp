package com.example.sugarsteps.user;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sugarsteps.DB.SugarStepsDataBase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * UserRepository is a data access layer that manages user-related operations
 * between the application and the underlying Room database.
 *
 * Features:
 * - Retrieve all users as LiveData.
 * - Insert a new user and return the generated user ID.
 * - Retrieve a user by their ID.
 * - Update existing user information.
 * - Delete a specific user or all users.
 *
 * @author Sivan Lasri
 * @version 5.0
 */
public class UserRepository {

    // DAO for user-related database operations
    private UserDao userDao;

    // LiveData list of all users in the database
    private LiveData<List<User>> allUsers;

    // ExecutorService to perform DB operations on background1 thread
    private ExecutorService executorService;

    /**
     * Constructor, initialize DB and DAO
     * **/
    public UserRepository(Application application) {
        // Get the singleton instance of the Room database
        SugarStepsDataBase db = SugarStepsDataBase.getDatabase(application);
        // Get the UserDao from the database
        userDao = db.usersDao();
        // Get LiveData of all users (auto-updated on DB changes)
        allUsers = userDao.getAllUsers();
        // Initialize a single-threaded executor for background1 operations
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Getting all Users
     *
     * @return LiveData list of all users
     * **/
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    /**
     * Inserts a new user into the database
     *
     * @return Future with the generated user ID
     * **/
    public Future<Long> insertAndReturnId(final User user) {
        return executorService.submit(() -> userDao.insertUser(user));
    }

    /**
     * Updates an existing user record in the database (in background)
     * **/
    public void updateUser(User user) {
        executorService.execute(() -> userDao.updateUser(user));
    }

    /**
     * Retrieves a user by their ID as LiveData.
     *
     * @return Id of the User
     * **/
    public LiveData<User> getUserById(long id) {
        return userDao.getUserById(id);
    }

    /**
     * Deletes a specific user from the database.
     * **/
    public void delete(final User user) {
        executorService.execute(() -> userDao.deleteUser(user));
    }

    /**
     * Deletes all users from the database.
     * **/
    public void deleteAll() {
        executorService.execute(userDao::deleteAllUsers);
    }
}
