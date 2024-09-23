package arc.haldun.mylibrary.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import arc.haldun.database.Sorting;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.Tools.Preferences;
import arc.haldun.mylibrary.main.SplashScreenActivity;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.views.CardItem;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    CardItem cardLanguage, cardAccount, cardSortBooks, cardTheme, cardAppInfo, cardCredits;

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

        cardAppInfo.addSubTittle(BuildConfig.VERSION_NAME);

        cardLanguage.setOnClickListener(this);
        cardAccount.setOnClickListener(this);
        cardSortBooks.setOnClickListener(this);
        cardTheme.setOnClickListener(this);
        cardAppInfo.setOnClickListener(this);
        cardCredits.setOnClickListener(this);
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
        cardTheme = findViewById(R.id.activity_settings_item_theme);
        cardAppInfo = findViewById(R.id.activity_settings_item_app_info);
        cardCredits = findViewById(R.id.activity_settings_item_credits);

        preferencesTool = new Tools.Preferences(
                getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE));
    }

    @Override
    public void onClick(View view) {

        if (view.equals(cardLanguage)) showLanguageOptionsDialog();

        if (view.equals(cardAccount))
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));

        if (view.equals(cardSortBooks)) showSortingOptionsDialog();

        if (view.equals(cardTheme)) showThemeOptionsDialog();

        if (view.equals(cardAppInfo)) showAppInfo();

        if (view.equals(cardCredits)) showCredits();
    }

    private void showCredits() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Geliştirici: ").append("Haldun Altuntop").append("\n\n");
        stringBuilder.append("İletişim").append("\n")
                .append("\t\t").append("Telefon: ").append("+90 542 112 55 78").append("\n")
                .append("\t\t").append("E-Posta: ").append("altuntophaldun@gmail.com").append("\n\n");
        stringBuilder.append("Bizi tercih ettiğiniz için teşekkür ederiz :)");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle("Credits")
                .setMessage(stringBuilder.toString())
                .setPositiveButton(getString(R.string.ok), null);

        alertDialogBuilder.create().show();
    }

    private void showAppInfo() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Version: ").append(BuildConfig.VERSION_NAME).append("\n");
        stringBuilder.append("Version Code: ").append(BuildConfig.VERSION_CODE).append("\n");
        stringBuilder.append("Yayın Türü: ").append(BuildConfig.BUILD_TYPE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle("Application Information")
                .setMessage(stringBuilder.toString())
                .setPositiveButton(getString(R.string.ok), null);

        alertDialogBuilder.create().show();

    }

    private void showThemeOptionsDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle("Temalar")
                .setMessage(getString(R.string.not_supported_yet))
                .setPositiveButton(getString(R.string.ok), null);

        alertDialogBuilder.create().show();
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

            case A_TO_Z_AUTHOR:
                return getString(R.string.a_to_z_author_name);

            case Z_TO_A_AUTHOR:
                return getString(R.string.z_to_a_author_name);

            case MORE_POPULAR:
                return getString(R.string.more_popular);

            case LESS_POPULAR:
                return getString(R.string.less_popular);

            default:
                RuntimeException exception = new RuntimeException("Geçersiz Sorting türü. A'dan Z'ye Sorting türüne ayarlanıyor.");
                exception.printStackTrace();
                Tools.makeText(getApplicationContext(), exception.getMessage());
                return getString(R.string.a_to_z_book_name);

        }

    }

    private void showSortingOptionsDialog() {

        final int[] selectedSortingType = new int[1];

        String[] options = {
                getString(R.string.a_to_z_book_name),
                getString(R.string.z_to_a_book_name),
                getString(R.string.old_to_new),
                getString(R.string.new_to_old),
                getString(R.string.a_to_z_author_name),
                getString(R.string.z_to_a_author_name),
                getString(R.string.more_popular),
                getString(R.string.less_popular)
        };

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