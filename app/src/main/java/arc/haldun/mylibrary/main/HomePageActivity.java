package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.settings.SettingsActivity;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar actionBar;
    private CardView cardProfile, cardSettings;

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

    }

    private void init() {
        actionBar = findViewById(R.id.activity_home_page_actionbar);

        cardProfile = findViewById(R.id.activity_home_page_cardview_profile);
        cardSettings = findViewById(R.id.activity_home_page_cardview_settings);
    }
}