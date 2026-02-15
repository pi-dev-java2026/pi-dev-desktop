package Fintech.utils;

import Fintech.entities.User;

/**
 * Singleton class to manage the current user session
 */
public class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the singleton instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if the current user is an administrator
     */
    public boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Clear the current session (logout)
     */
    public void clearSession() {
        currentUser = null;
    }
}
