package arc.haldun.mylibrary.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import arc.haldun.database.Sorting;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.Tools.Preferences;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.views.CardItem;
import arc.haldun.mylibrary.views.ViewSettingItem;
import arc.haldun.mylibrary.main.SplashScreenActivity;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.main.profile.ProfileActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    CardItem cardLanguage, cardAccount, cardSortBooks, cardTheme; // TODO: Init 'cardTheme'

    Toolbar actionbar;

    Tools.Preferences preferencesTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init(); // Init views

        //
        // Init actionbar
        //
        setSupportActionBar(actionbar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(getString(R.string.settings));
            supportActionbar.setDisplayHomeAsUpEnabled(true);
        }

        String lang = preferencesTool.getString(Tools.Preferences.Keys.LANGUAGE);
        cardLanguage.setSubTittle(lang);

        Sorting sorting = Sorting.valueOf(preferencesTool.getInt(Preferences.Keys.BOOK_SORTING_TYPE));
        cardSortBooks.setSubTittle(getSortingStringValue(sorting));

        cardAccount.setTittle(CurrentUser.user.getName());
        cardAccount.setSubTittle(CurrentUser.user.getPassword());

        cardLanguage.setOnClickListener(this);
        cardAccount.setOnClickListener(this);
        cardSortBooks.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        dialogBuilder = null;
        dialog = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {

        actionbar = findViewById(R.id.activity_settings_actionbar);

        cardAccount = findViewById(R.id.activity_settings_item_account);
        cardLanguage = findViewById(R.id.activity_settings_item_language);
        cardSortBooks = findViewById(R.id.activity_settings_item_sorting);

        preferencesTool = new Tools.Preferences(
                getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE));
    }

    @Override
    public void onClick(View view) {

        if (view.equals(cardLanguage)) showLanguageOptionsDialog();

        if (view.equals(cardAccount))
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));

        if (view.equals(cardSortBooks)) showSortingOptionsDialog();
    }

    private String getSortingStringValue(Sorting sorting) {

        switch (sorting) {

            case A_TO_Z:
                return getString(R.string.a_to_z_book_name);

            case Z_TO_A:
                return getString(R.string.z_to_a_book_name);

            case OLD_TO_NEW:
                return getString(R.string.old_to_new);

            case NEW_TO_OLD:
                return getString(R.string.new_to_old);

            default:
                RuntimeException exception = new RuntimeException("Geçersiz Sorting türü. A'dan Z'ye Sorting türüne ayarlanıyor.");
                exception.printStackTrace();
                return getString(R.string.a_to_z_book_name);

        }

    }

    private void showSortingOptionsDialog() {

        final int[] selectedSortingType = new int[1];

        String[] options = {getString(R.string.a_to_z_book_name), getString(R.string.z_to_a_book_name),
                getString(R.string.old_to_new), getString(R.string.new_to_old),
                getString(R.string.a_to_z_author_name), getString(R.string.z_to_a_author_name)};

        int currentSortingType = preferencesTool.getInt(Tools.Preferences.Keys.BOOK_SORTING_TYPE);
        selectedSortingType[0] = Sorting.valueOf(currentSortingType).getIndex();

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle(getString(R.string.sort))

                .setSingleChoiceItems(
                        options,
                        Sorting.valueOf(currentSortingType).getIndex(),
                        (dialogInterface, i) -> selectedSortingType[0] = i)

                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {

                    Sorting selectedType = Sorting.valueOf(selectedSortingType[0]);

                    preferencesTool.setValue(
                            Preferences.Keys.BOOK_SORTING_TYPE,
                            selectedType.getIndex());

                    cardSortBooks.setSubTittle(getSortingStringValue(selectedType));
                });

        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void showLanguageOptionsDialog() {

        final int[] selectedLanguage = new int[1];

        String[] languages = {getString(R.string.turkish), getString(R.string.english), getString(R.string.german)};

        String currentLanguage = preferencesTool.getString(Tools.Preferences.Keys.LANGUAGE);

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle(getString(R.string.language))
                .setSingleChoiceItems(languages, Language.getCode(currentLanguage),
                        (dialogInterface, i) -> selectedLanguage[0] = i)
                .setPositiveButton("OK", (dialogInterface, i) -> {

                    Preferences preferencesTool = new Preferences(
                            getSharedPreferences(Preferences.NAME, Context.MODE_PRIVATE));

                    preferencesTool.setValue(Preferences.Keys.LANGUAGE,
                            Language.getLanguage(selectedLanguage[0]));

                    startActivity(new Intent(SettingsActivity.this,
                            SplashScreenActivity.class));
                })
                .setNegativeButton(getString(R.string.cancel), null);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public static class Language {
        public final static int TURKISH = 0;
        public final static int ENGLISH = 1;
        public final static int GERMAN = 2;

        public static String getLanguage(int language) {

            switch (language) {

                case ENGLISH:
                    return "en";

                case GERMAN:
                    return "de";

                default:
                    return "tr";
            }
        }

        public static int getCode(String lang) {

            switch (lang) {

                case "en":
                    return ENGLISH;

                case "de":
                    return GERMAN;

                default:
                    return TURKISH;
            }
        }
    }
}