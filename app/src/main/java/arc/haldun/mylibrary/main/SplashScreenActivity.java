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
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import arc.haldun.database.DatabaseConfig;
import arc.haldun.database.Sorting;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.driver.Connector;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.exceptions.UncaughtExceptionHandler;
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
    HandlerThread splashScreenThread;
    public static Runnable rInitLanguage, rConnectDatabase, rCheckUpdates, rCheckUser, rNetwork;
    boolean hasUpdate = false;
    boolean isConnected;

    String url = "https://drive.google.com/drive/folders/1Wd95pcUJ6UDVsEp_xYjDrQrEwk3fJvCm?usp=sharing";

    public static User[] users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();

        splashScreenThread.start();

        handlerBackground = new Handler(splashScreenThread.getLooper());

        // Animasyonun tamamlanmasını bekliyoruz (1 saniye)
        handlerBackground.postDelayed(rNetwork, 1000);

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

        Thread.setDefaultUncaughtExceptionHandler(
                new UncaughtExceptionHandler(getApplicationContext()));

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

            if (!isConnected) {
                Tools.startErrorActivity(
                        getApplicationContext(),
                        new Exception("Veritabanına bağlanılamadı.")
                );
                return;
            }

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
                isConnected = databaseConfig.connect();
            } catch (IOException e) {
                Tools.startErrorActivity(getApplicationContext(), e);
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

            /**New Code**/
            Intent intHomePageActivity = new Intent(getApplicationContext(), HomePageActivity.class);
            intLibraryActivity.putExtra("hasUpdate", hasUpdate);
            intLibraryActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            /**New Code**/

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
                        //startActivity(intLibraryActivity);

                        Log.i("SplashScreen", "Library Activity is deprecated. Starting Home Page Activity.");
                        startActivity(intHomePageActivity);

                        // Update last seen
                        CurrentUser.user.setLastSeen(new DateTime());
                        new MariaDB().updateUser(CurrentUser.user);
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
                
                finish();
            });

        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        splashScreenThread.quit();
    }
}