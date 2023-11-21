package arc.haldun.mylibrary.main;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import arc.haldun.database.DatabaseConfig;
import arc.haldun.database.Sorting;
import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.driver.Connector;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.main.LibraryActivity;
import arc.haldun.mylibrary.services.FirebaseUserService;
import arc.haldun.mylibrary.settings.SettingsActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    TextView textView;

    ProgressBar progressBar;

    AlertDialog dialog;
    AlertDialog.Builder dialogBuilder;
    DialogInterface.OnClickListener onDialogPositiveClick, onDialogNegativeClick;

    Thread threadMain, threadNetwork;
    Handler handlerMain, handlerBackground;

    Tools.Update update;
    //mObservable server;
    //mObserver client;

    //FirebaseAuth firebaseAuth;
    //FirebaseUser firebaseUser;
    HandlerThread splashScreenThread;
    Runnable rInitLanguage, rConnectDatabase, rCheckUpdates, rCheckUser, rNetwork;
    boolean hasUpdate = false;

    public static User[] users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();

        splashScreenThread.start();

        handlerBackground = new Handler(splashScreenThread.getLooper());

        handlerBackground.post(rNetwork);

        Tools.Preferences preferencesTool = new Tools.Preferences(getSharedPreferences(
                Tools.Preferences.NAME, Context.MODE_PRIVATE));

        try {

            int sortingStringValue = preferencesTool
                    .getInt(Tools.Preferences.Keys.BOOK_SORTING_TYPE);

            if (sortingStringValue == -1) {

                preferencesTool.setValue(Tools.Preferences.Keys.BOOK_SORTING_TYPE, Sorting.OLD_TO_NEW.getIndex());

            }

        } catch (RuntimeException e) {
            e.printStackTrace();
            preferencesTool.setValue(Tools.Preferences.Keys.BOOK_SORTING_TYPE, Sorting.OLD_TO_NEW.getIndex());
        }

    }

    private void setProcess(String name, int percent) {

        handlerMain.post(() -> {

            textView.setText(name);
            progressBar.setProgress(percent);

        });

    }

    private void init() {

        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.activity_splashscreen_progressBar);

        //server = new mObservable();
        //client = new mObserver();
        handlerMain = new Handler(Looper.getMainLooper());

        //firebaseAuth = FirebaseAuth.getInstance();
        //firebaseUser = firebaseAuth.getCurrentUser();

        splashScreenThread = new HandlerThread("SplashScreenActivityThread");

        //
        // Init dialog
        //
        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.update))
                .setMessage(getString(R.string.new_version_released))
                .setPositiveButton(getString(R.string.download), onDialogPositiveClick)
                .setNegativeButton(getString(R.string.cancel), onDialogNegativeClick);

        dialog = dialogBuilder.create();

        onDialogPositiveClick = (dialogInterface, i) -> {

            //
            // Redirect download page
            //
            String url = "http://haldun.online";

            try {
                Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intentBrowser.addCategory(Intent.CATEGORY_BROWSABLE);
                intentBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBrowser);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        };

        /**
         * Runnable
         */

        rNetwork = () -> {

            setProcess("Initializing language", 20);
            rInitLanguage.run();

            setProcess("Connecting database", 40);
            rConnectDatabase.run();

            setProcess("Checking udates", 60);
            rCheckUpdates.run();

            setProcess("Check logged in user", 80);
            rCheckUser.run();

        };

        rInitLanguage = () -> {

            Tools.Preferences preferencesTool = new Tools.Preferences(
                    getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE));

            String language = preferencesTool.getString(Tools.Preferences.Keys.LANGUAGE);
            if (language == null || language.isEmpty() || language.equals("null")) {

                preferencesTool.setValue(Tools.Preferences.Keys.LANGUAGE,
                        SettingsActivity.Language.getLanguage(SettingsActivity.Language.TURKISH));

                language = SettingsActivity.Language.getLanguage(SettingsActivity.Language.TURKISH);
            }
            Locale locale = new Locale(language);
            Resources resources = getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        };

        rConnectDatabase = () -> {
            try {
                DatabaseConfig databaseConfig = DatabaseConfig.load();
                databaseConfig.connect();
            } catch (IOException e) {
                Tools.startErrorActivity(getApplicationContext(), e);
            } catch (ClassNotFoundException e) {
                Tools.startErrorActivity(
                        getApplicationContext(),
                        new ClassNotFoundException(getString(R.string.class_not_foud_exception_msg)));
            }
        };

        rCheckUpdates = () -> {

            try {

                String sql = "SELECT * FROM updates";
                Statement statement = Connector.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()) {

                    int versionCode = resultSet.getInt("VersionCode");

                    if (versionCode > BuildConfig.VERSION_CODE) {
                        hasUpdate = true;
                        break;
                    }
                }
            } catch (SQLException e) {
                Tools.startErrorActivity(this, e);
            }

        };

        rCheckUser = () -> {

            //Manager databaseManager = new Manager(new MariaDB());

            Intent intWelcomeActivity = new Intent(getApplicationContext(), WelcomeActivity.class);
            intWelcomeActivity.putExtra("hasUpdate", hasUpdate);
            intWelcomeActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            Intent intLibraryActivity = new Intent(getApplicationContext(), LibraryActivity.class);
            intLibraryActivity.putExtra("hasUpdate", hasUpdate);
            intLibraryActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            FirebaseUserService firebaseUserService = new FirebaseUserService();

            if (!firebaseUserService.hasLoggedInUser()) {

                // Redirect to Welcome Activity
                setProcess("Welcome", 100);
                startActivity(intWelcomeActivity);

            }

            firebaseUserService.isUserValid((b, user) -> {

                if (b) {

                    CurrentUser.user = user;

                    Tools.Preferences preferencesTool = new Tools.Preferences(
                            getSharedPreferences(Tools.Preferences.NAME, Context.MODE_PRIVATE));

                    boolean rememberMe = preferencesTool
                            .getBoolean(Tools.Preferences.Keys.REMEMBER_ME);

                    if (rememberMe) {
                        // Redirect to Library Activity
                        setProcess("Starting", 100);
                        startActivity(intLibraryActivity);
                    } else {

                        firebaseUserService.signOut();

                        // Redirect to Welcome Activity
                        setProcess("Welcome", 100);
                        startActivity(intWelcomeActivity);
                    }
                } else {

                    setProcess("Welcome", 100);

                    firebaseUserService.quit();

                    startActivity(intWelcomeActivity);

                }

            });

        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        splashScreenThread.quit();
    }

    /*
    class mObserver implements Observer {

        @Override
        public void update(Observable observable) {
            dialog.show();
        }
    }

    static class mObservable extends Observable {

        private int versionCode;

        @SuppressWarnings("unused")
        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
            check();
        }

        public void check() {

            if (versionCode > BuildConfig.VERSION_CODE) {
                notice();
            }
        }
    }

     */
}

/**
 * textView.setText("Initializing language");
 *
 *         //
 *         // Init language
 *         //
 *         PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));
 *         String language = preferencesTool.getString(PreferencesTool.Keys.LANGUAGE);
 *         if (language == null || language.isEmpty() || language.equals("null")) {
 *             preferencesTool.setValue(PreferencesTool.Keys.LANGUAGE, SettingsActivity.Language.getLanguage(SettingsActivity.Language.TURKISH));
 *             language = SettingsActivity.Language.getLanguage(SettingsActivity.Language.TURKISH);
 *         }
 *         Locale locale = new Locale(language);
 *         Resources resources = getResources();
 *         Configuration configuration = resources.getConfiguration();
 *         configuration.setLocale(locale);
 *         resources.updateConfiguration(configuration, resources.getDisplayMetrics());
 *
 *         progressBar.setProgress(20);
 *
 *         //
 *         // Init threads
 *         //
 *         threadMain = new Thread(() -> {
 *             threadNetwork.start();
 *             try {
 *                 threadNetwork.join();
 *                 progressBar.setProgress(100);
 *             } catch (InterruptedException e) {
 *                 throw new RuntimeException(e);
 *             }
 *         });
 *
 *         threadNetwork = new Thread(() -> {
 *             Looper.prepare();
 *             runOnUiThread(() -> textView.setText("Connecting to database"));
 *
 *             //
 *             // Connect database
 *             //
 *             try {
 *                 DatabaseConfig databaseConfig = DatabaseConfig.load();
 *                 databaseConfig.connect();
 *             } catch (IOException | ClassNotFoundException e) {
 *                 throw new RuntimeException(e);
 *             }
 *
 *             progressBar.setProgress(40);
 *
 *             Manager manager = new Manager(new MariaDB());
 *             SplashScreenActivity.users = manager.selectUser();
 *
 *             //
 *             // Check updates
 *             //
 *             runOnUiThread(()-> textView.setText("Checking updates"));
 *
 *             boolean hasUpdate = false;
 *
 *             try {
 *
 *                 String sql = "SELECT * FROM updates";
 *                 Statement statement = Connector.connection.createStatement();
 *                 ResultSet resultSet = statement.executeQuery(sql);
 *
 *                 while (resultSet.next()) {
 *
 *                     int versionCode = resultSet.getInt("VersionCode");
 *
 *                     if (versionCode > BuildConfig.VERSION_CODE) {
 *                         hasUpdate = true;
 *                         break;
 *                     }
 *                 }
 *             } catch (SQLException e) {
 *                 Tools.startErrorActivity(this, e);
 *             }
 *
 *             progressBar.setProgress(60);
 *
 *             //
 *             // Check user
 *             //
 *             runOnUiThread(()-> textView.setText("Checking logged in user"));
 *
 *             firebaseUser = firebaseAuth.getCurrentUser();
 *             if (firebaseUser != null) {
 *
 *                 CurrentUser.user = new MariaDB().setExceptionListener(new IDatabase.IExceptionListener() {
 *                     @Override
 *                     public void onException(Exception e) {
 *                         e.printStackTrace();
 *                     }
 *                 }).getUser(firebaseUser.getUid());
 *                 if (CurrentUser.user == null) {
 *                     firebaseAuth.signOut();
 *
 *                 } else {
 *                     runOnUiThread(() -> textView.setText("Starting"));
 *
 *                     startActivity(new Intent(SplashScreenActivity.this, LibraryActivity.class)
 *                                     .putExtra("hasUpdate", hasUpdate));
 *                 }
 *             } else {
 *                 runOnUiThread(() -> textView.setText("Welcome"));
 *
 *                 startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class)
 *                         .putExtra("hasUpdate", hasUpdate));
 *             }
 *             progressBar.setProgress(80);
 *
 *             finish();
 *         });
 *
 *         threadMain.start();
 */