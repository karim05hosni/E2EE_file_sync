package com.karimhosny.auth.api;

import com.karimhosny.auth.entities.User;

public class UserSession {
    private static UserSession instance;

    private User currentUser;

    private UserSession() {
        // private constructor to prevent external instantiation
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // public void logout() {
    //     if (currentUser != null) {
    //         // wipe sensitive data
    //         if (currentUser.getUmk() != null)
    //             Arrays.fill(currentUser.getUmk(), (byte) 0);

    //         // wipe private key if decrypted
    //         if (currentUser.getWrappedPrivK() != null && currentUser.getWrappedPrivK().getDecryptedBytes() != null)
    //             Arrays.fill(currentUser.getWrappedPrivK().getDecryptedBytes(), (byte) 0);

    //         currentUser = null;
    //     }
    // }


}

