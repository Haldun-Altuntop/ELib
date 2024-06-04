package arc.haldun.mylibrary.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.services.FirebaseUserService;
import arc.haldun.mylibrary.settings.SettingsActivity;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar actionBar;
    private CardView cardProfile, cardSettings, cardSearch, cardFriends, cardLogout, cardRequests;

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

        init();

        // Set action bar
        setSupportActionBar(actionBar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(R.string.app_name);
            supportActionbar.setDisplayHomeAsUpEnabled(true);
        }

        cardProfile.setOnClickListener(this);
        cardSettings.setOnClickListener(this);
        cardFriends.setOnClickListener(this);
        cardSearch.setOnClickListener(this);
        cardLogout.setOnClickListener(this);
        cardRequests.setOnClickListener(this);

        if (Objects.equals(CurrentUser.user.getPriority(), User.USER)) {
            cardRequests.setVisibility(View.INVISIBLE);
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

    private void startSuspendedActivity() {

        startActivity(new Intent(getApplicationContext(), SuspendedActivity.class));

        Toast.makeText(this, "Yasaklı kullanıcı", Toast.LENGTH_LONG).show();
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

        new FirebaseUserService().signOut();

        CurrentUser.user = null;

        // Prepare intent
        Intent intWelcomeActivity = new Intent(getApplicationContext(), WelcomeActivity.class);
        startActivity(intWelcomeActivity);

        finish();
    }

    private void init() {
        actionBar = findViewById(R.id.activity_home_page_actionbar);

        cardProfile = findViewById(R.id.activity_home_page_cardview_profile);
        cardSettings = findViewById(R.id.activity_home_page_cardview_settings);
        cardSearch = findViewById(R.id.activity_home_page_cardview_search);
        cardFriends = findViewById(R.id.activity_home_page_cardview_friends);
        cardLogout = findViewById(R.id.activity_home_page_cardview_logout);
        cardRequests = findViewById(R.id.activity_home_page_cardview_requests);
    }
}