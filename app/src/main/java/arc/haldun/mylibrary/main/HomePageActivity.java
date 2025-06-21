package arc.haldun.mylibrary.main;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.server.api.ELibUtilities;
import arc.haldun.mylibrary.server.api.UnauthorizedUserException;
import arc.haldun.mylibrary.services.FirebaseUserService;
import arc.haldun.mylibrary.settings.SettingsActivity;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    private Manager databaseManager;
    private Toolbar actionBar;
    private CardView cardProfile, cardSettings, cardSearch, cardFriends, cardLogout, cardRequests;

    private Runnable rNewVersionAvailable;

    private FirebaseUserService firebaseUserService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUser();
        init();
        checkUpdates();

        // Set action bar
        setSupportActionBar(actionBar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(R.string.app_name);
            supportActionbar.setDisplayHomeAsUpEnabled(false);
        }

        cardProfile.setOnClickListener(this);
        cardSettings.setOnClickListener(this);
        cardFriends.setOnClickListener(this);
        cardSearch.setOnClickListener(this);
        cardLogout.setOnClickListener(this);
        cardRequests.setOnClickListener(this);

        if (Objects.equals(CurrentUser.user.getPriority(), User.Priority.USER)) {
            cardRequests.setVisibility(View.INVISIBLE);
        }

        updateClientVersion();
    }

    private void checkUpdates() {

        new Thread(() -> {

            try {
                int versionCode = ELibUtilities.getVersionCode();

                if (versionCode > BuildConfig.VERSION_CODE) {
                    Log.i("SplashScreen", "New version available (" + versionCode + ")");

                    runOnUiThread(rNewVersionAvailable);
                }
            } catch (UnauthorizedUserException e) {
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                finish();
                e.printStackTrace(System.err);
            }
        }).start();
    }

    private void initUser() {

        Thread t = new Thread(() -> {

            try {

                JSONObject userJson = ELibUtilities.getUser();

                CurrentUser.user = new User(userJson);

            } catch (UnauthorizedUserException e) {
                throw new RuntimeException(e);
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CurrentUser.user.isSuspended()) {
            startSuspendedActivity();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //
        // Check remember me
        //
        Tools.Preferences preferencesTool = new Tools.Preferences(
                getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE));
        boolean rememberMe = preferencesTool.getBoolean(Tools.Preferences.Keys.REMEMBER_ME);

        if (!rememberMe) {

            new Thread(() -> {

                try {
                    ELibUtilities.quit();
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }

            }).start();
        }
    }

    private void updateClientVersion() {

        if (firebaseUserService.hasLoggedInUser()) { // Giriş yapmış kullanıcı varsa

            String uid = firebaseUserService.getFirebaseUser().getUid();
            new Thread(() -> {
                CurrentUser.user = databaseManager.getUser(uid); // CurrentUser sınıfını başlat

                //
                // Set client version
                //
                databaseManager.updateClientVersion(CurrentUser.user, String.valueOf(BuildConfig.VERSION_CODE));
            }).start();
        } else if (CurrentUser.user != null) {

            Log.e("LibraryActivity", "Currentuser.user null idi");

        } else { // Giriş yapmış kullanıcı yoksa

            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent); // WelcomeActivity'ye yönlendir
        }
    }

    private void startSuspendedActivity() {

        startActivity(new Intent(getApplicationContext(), SuspendedActivity.class));

        Toast.makeText(this, getString(R.string.suspended_user), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

        if (v.equals(cardProfile)) {
            // Prepare intent
            Intent intProfileActivity = new Intent(getApplicationContext(), ProfileActivity.class);

            startActivity(intProfileActivity);

        }

        if (v.equals(cardSettings)) {
            // Prepare intent
            Intent intSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);

            startActivity(intSettingsActivity);
        }

        if (v.equals(cardSearch)) {
            // Prepare intent
            Intent intLibraryActivity = new Intent(getApplicationContext(), LibraryActivity.class);

            startActivity(intLibraryActivity);
        }

        if (v.equals(cardFriends)) {
            Tools.makeText(getApplicationContext(), getString(R.string.not_supported_yet));
            Log.e("Friends", "Arkadaşlar özelliği desteklenmiyor. Bunu düzelt.");
        }

        if (v.equals(cardLogout)) logout();

        if (v.equals(cardRequests)) {
            // Prepare intent
            Intent intLibraryActivity = new Intent(getApplicationContext(), RequestsActivity.class);

            startActivity(intLibraryActivity);
        }
    }

    private void logout() {
        Tools.Preferences preferencesTool = new Tools.Preferences(
                getSharedPreferences(
                        Tools.Preferences.NAME,
                        Context.MODE_PRIVATE
                )
        );
        preferencesTool.setValue(Tools.Preferences.Keys.REMEMBER_ME, false);

        firebaseUserService.signOut();


        new Thread(() -> {
            try {
                ELibUtilities.quit();
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Prepare intent
        Intent intWelcomeActivity = new Intent(getApplicationContext(), WelcomeActivity.class);
        startActivity(intWelcomeActivity);

        finish();
    }

    private void init() {
        databaseManager = new Manager(new MariaDB());

        actionBar = findViewById(R.id.activity_home_page_actionbar);

        cardProfile = findViewById(R.id.activity_home_page_cardview_profile);
        cardSettings = findViewById(R.id.activity_home_page_cardview_settings);
        cardSearch = findViewById(R.id.activity_home_page_cardview_search);
        cardFriends = findViewById(R.id.activity_home_page_cardview_friends);
        cardLogout = findViewById(R.id.activity_home_page_cardview_logout);
        cardRequests = findViewById(R.id.activity_home_page_cardview_requests);

        rNewVersionAvailable = () -> {
            DialogInterface.OnClickListener onDialogPositiveClick = (dialogInterface, i) -> {

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

                    Log.e("LibraryActivity", "", e);

                }
            };

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HomePageActivity.this);
            dialogBuilder.setTitle(getString(R.string.update))
                    .setMessage(getString(R.string.new_version_released))
                    .setPositiveButton(getString(R.string.download), onDialogPositiveClick)
                    .setNegativeButton(getString(R.string.cancel), null);

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        };

        firebaseUserService = new FirebaseUserService();
    }
}