package com.example.sugarsteps.user;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Future;
/**
 * ViewModel that survives configuration changes and is tied to the application context.
 *
 * Features:
 * - Get all Users
 * - Insert User and get UserID
 * - Updating User
 * - Deleting User / All Users
 *
 * @author Sivan Lasri
 * @version 5.0
 */
public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepository;         // Reference to the repository layer
    private LiveData<List<User>> allUsers;          // LiveData holding the list of all users

    private MutableLiveData<Long> insertedUserId = new MutableLiveData<>();     // LiveData used to expose the ID of a newly inserted user

    /**
     * Constructor, initialize and loading all users from UserRepository
     * **/
    public UserViewModel(Application application) {
        super(application);  // Pass application context to the superclass
        userRepository = new UserRepository(application);  // Initialize repository with context
        allUsers = userRepository.getAllUsers();            // Load all users from repository
    }

    /**
     * Getting all Users from LiveData
     *
     * @return LiveData list of all users
     * **/
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    /**
     * Inserting User and Getting UserID
     *
     * @return UserID
     * **/
    public LiveData<Long> getInsertedUserId() {
        return insertedUserId;
    }

    /**
     * Updating User (when changing fields)
     * **/
    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    /**
     * Inserting User to the repository with thread for non blocking ThreadUI
     * **/
    public void insert(User user) {
        // Insert a user and return a Future that will eventually hold the inserted ID
        Future<Long> futureId = userRepository.insertAndReturnId(user);

        // Create a new thread to handle the Future result (non-blocking for UI)
        new Thread(() -> {
            try {
                Long id = futureId.get();              // Wait for the insert operation to complete
                insertedUserId.postValue(id);          // Update LiveData with the inserted ID
            } catch (Exception e) {
                e.printStackTrace();                   // Log any exception that occurs
                insertedUserId.postValue(-1L);         // Use -1 to indicate an error occurred
            }
        }).start();  // Start the thread
    }

    /**
     * Getting User by UserID
     *
     * @return User from UserRepository by field ID
     * **/
    public LiveData<User> getUserById(long id) {
        // Request a specific user by ID from the repository
        return userRepository.getUserById(id);
    }

    /**
     * Deleting User by field User
     * **/
    public void deleteUser(User user) {
        // Delete the given user via the repository
        userRepository.delete(user);
    }

    /**
     * Deleting all users from the repository
     * **/
    public void deleteAll() {
        // Delete all users via the repository
        userRepository.deleteAll();
    }
}
