package com.warehousewms.util;

import java.util.prefs.Preferences;

public class CredentialStorage {
    private static final Preferences prefs = Preferences.userNodeForPackage(CredentialStorage.class);
    private static final String KEY_USERNAME = "remembered_username";
    private static final String KEY_PASSWORD = "remembered_password";

    public static void saveCredentials(String username, String password) {
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_PASSWORD, EncryptionUtil.encrypt(password));
    }

    public static String[] getSavedCredentials() {
        String username = prefs.get(KEY_USERNAME, null);
        String encryptedPassword = prefs.get(KEY_PASSWORD, null);
        if (username != null && encryptedPassword != null) {
            String password = EncryptionUtil.decrypt(encryptedPassword);
            if (password != null) {
                return new String[]{ username, password };
            }
        }
        return null;
    }

    public static void clearCredentials() {
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_PASSWORD);
    }
}