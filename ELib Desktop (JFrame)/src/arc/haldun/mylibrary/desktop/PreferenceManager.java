package arc.haldun.mylibrary.desktop;

import arc.haldun.database.Sorting;

import java.io.*;
import java.util.Properties;

public class PreferenceManager {

    private static final String FILE_NAME = "preferences.properties";

    private final static String KEY_REMEMBER_ME = "remember-me";
    public static boolean rememberMe;

    private static final String KEY_BOOK_SORTING = "sorting";
    public static Sorting bookSorting;

    private static final String KEY_SHOW_BUSY_BOOKS = "show-busy-books";
    public static boolean showBusyBooks;

    public static void loadPreferences() {

        try {

            File file = new File(FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();

                setBookSorting(Sorting.OLD_TO_NEW);
            }

            InputStream preferencesStream = new FileInputStream(file);
            Properties preferences = new Properties();

            preferences.load(preferencesStream);

            rememberMe = Boolean.parseBoolean(preferences.getProperty(KEY_REMEMBER_ME, String.valueOf(false)));
            bookSorting = Sorting.valueOf(Integer.parseInt(preferences.getProperty(KEY_BOOK_SORTING, String.valueOf(Sorting.A_TO_Z.getIndex()))));
            showBusyBooks = Boolean.parseBoolean(preferences.getProperty(KEY_SHOW_BUSY_BOOKS, String.valueOf(false)));

            preferencesStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void setRememberMe(boolean rememberMe) {
        PreferenceManager.rememberMe = rememberMe;

        try {

            InputStream preferencesStream = new FileInputStream(FILE_NAME);
            Properties preferences = new Properties();

            preferences.load(preferencesStream);
            preferences.setProperty(KEY_REMEMBER_ME, String.valueOf(rememberMe));
            preferences.store(new FileOutputStream(FILE_NAME), "Preferences for E-Lib");

            preferencesStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void setBookSorting(Sorting sorting) {
        PreferenceManager.bookSorting = sorting;

        try {

            InputStream preferencesStream = new FileInputStream(FILE_NAME);
            Properties preferences = new Properties();

            preferences.load(preferencesStream);
            preferences.setProperty(KEY_BOOK_SORTING, String.valueOf(sorting.getIndex()));
            preferences.store(new FileOutputStream(FILE_NAME), "Preferences for E-Lib");

            preferencesStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setShowBusyBooks(boolean showBusyBooks) {
        PreferenceManager.showBusyBooks = showBusyBooks;

        try {

            InputStream preferencesStream = new FileInputStream(FILE_NAME);
            Properties preferences = new Properties();

            preferences.load(preferencesStream);
            preferences.setProperty(KEY_SHOW_BUSY_BOOKS, String.valueOf(showBusyBooks));
            preferences.store(new FileOutputStream(FILE_NAME), "Preferences for E-Lib");

            preferencesStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
