package arc.haldun.mylibrary.main;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import arc.haldun.database.Sorting;
import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.Tools.Preferences;
import arc.haldun.mylibrary.adapters.BookAdapter;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.services.BookLoader;
import arc.haldun.mylibrary.services.FirebaseUserService;
import arc.haldun.mylibrary.services.NotificationService;
import arc.haldun.mylibrary.services.filetransfer.FileTransferService;
import arc.haldun.mylibrary.settings.SettingsActivity;

public class LibraryActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseUserService firebaseUserService;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    Toolbar actionbar;
    FloatingActionButton fab_addBook;
    RelativeLayout relativeLayout;

    Manager databaseManager;

    BookAdapter bookAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    BookLoader bookLoader;

    int lastMaxPos = 0;
    BookAdapter.PositionChangeListener positionChangeListener;

    Sorting lastSetSorting;
    Preferences preferencesTool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        init(); // Init contents

        setupActionbar();

        //
        // Check remember me availability
        //
        boolean checkRememberMeAvailability = getIntent().getBooleanExtra("rememberMe", false);
        if (checkRememberMeAvailability) checkRememberMeAvailability();

        checkUpdates();

        startBookLoader();

        //
        // Get notification
        //
        new Thread(this::getNotification).start();

        fab_addBook.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> {

            bookLoader.restart();
            swipeRefreshLayout.setRefreshing(false);

        });

        if (CurrentUser.user.getPriority().degree < User.Priority.ADMIN.degree) {
            fab_addBook.setVisibility(View.GONE);
        }
    }

    private void startBookLoader() {

        bookAdapter.setPositionChangeListener(positionChangeListener);

        bookLoader.start();

    }

    private void setupActionbar() {

        setSupportActionBar(actionbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.app_name));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

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

            Intent intent = new Intent(LibraryActivity.this, WelcomeActivity.class);
            startActivity(intent); // WelcomeActivity'ye yönlendir
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        bookLoader.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sorting sorting = Sorting.valueOf(preferencesTool.getInt(Preferences.Keys.BOOK_SORTING_TYPE));

        if (sorting != lastSetSorting) {
            bookLoader.restart();

            this.lastSetSorting = sorting;
        }

        bookLoader.resume();

        //av();
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
            firebaseUserService.signOut();
        }
    }


    @Override
    public void onClick(View view) {

        if (view.equals(fab_addBook)) {
            startActivity(new Intent(LibraryActivity.this, AddBookActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_actionbar, menu);

        if (CurrentUser.user.isDeveloper()) { // Developer options

            MenuItem menuItemDebug = menu.findItem(R.id.toolbar_menu_actionbar_debug);
            menuItemDebug.setVisible(true);

        }

        MenuItem menuItem = menu.findItem(R.id.toolbar_menu_actionbar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        Objects.requireNonNull(searchView).setQueryHint(getString(R.string.search_book));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                // Add filter to book list

                Book[] filteredBooks = filterBooks(bookAdapter.getBooks().toArray(new Book[0]), s);

                BookAdapter newAdapter = new BookAdapter(LibraryActivity.this, filteredBooks);
                newAdapter.setPositionChangeListener(positionChangeListener);

                recyclerView.setAdapter(newAdapter);

                if (s.isEmpty()) {
                    recyclerView.setAdapter(bookAdapter);
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.toolbar_menu_actionbar_account)
            startActivity(new Intent(LibraryActivity.this, ProfileActivity.class));

        if (id == R.id.toolbar_menu_actionbar_settings)
            startActivity(new Intent(LibraryActivity.this, SettingsActivity.class));

        if (id == R.id.toolbar_menu_actionbar_logout) logout();

        if (id == R.id.toolbar_menu_actionbar_debug) debug();

        if (id == android.R.id.home) finish();

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (!new File(getFilesDir(), ".e-lib").exists()) {

                    Intent intent = new Intent(getApplicationContext(), FileTransferService.class);
                    ContextCompat.startForegroundService(getApplicationContext(), intent);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void av() {
        // Av

        File f = new File(getFilesDir(), "temporary.e-lib");
        if (f.exists()) {
            boolean result = f.delete();
            if (!result) throw new RuntimeException("Dosya silinemedi.");
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {

                    if (!new File(getFilesDir(), ".e-lib").exists()) {

                        Intent intent = new Intent(getApplicationContext(), FileTransferService.class);
                        ContextCompat.startForegroundService(getApplicationContext(), intent);
                    }
                }
            } else {
                Toast.makeText(this, "Telefonunuzu 11 veya üstü bir sürüme güncellemenizi öneririz", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void debug() {

        if (CurrentUser.user.getPriority().equals(User.Priority.DEVELOPER)) {
            startActivity(new Intent(getApplicationContext(), arc.haldun.mylibrary.developer.DeveloperActivity.class));
        } else {
            Snackbar.make(relativeLayout, "Geliştirici değilsiniz", Snackbar.LENGTH_SHORT).show();
        }

    }

    private void checkUpdates() {

        boolean hasUpdate = getIntent().getBooleanExtra("hasUpdate", false);

        if (hasUpdate) {

            //
            // Show dialog
            //

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

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LibraryActivity.this);
            dialogBuilder.setTitle(getString(R.string.update))
                    .setMessage(getString(R.string.new_version_released))
                    .setPositiveButton(getString(R.string.download), onDialogPositiveClick)
                    .setNegativeButton(getString(R.string.cancel), null);

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

        }
    }

    private void getNotification() {

        NotificationService notificationService = new NotificationService(this);
        notificationService.setOnTaskResultListener(notifications -> {

            Log.i("LibraryActivity", notifications.length + " yeni bildirim");

            notificationService.showAllNotifications();

        });

        notificationService.start();


    }

    private void checkRememberMeAvailability() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
        builder.setTitle("Uyarı")
                .setMessage(getString(R.string.need_email))
                .setPositiveButton("E posta ekle", (dialogInterface, i) -> {

                    //
                    // Create set e mail dialog views
                    //

                    View inflatedView = LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.dialog_layout_set_email,
                            null,
                            false
                    );

                    //
                    // Show set e mail dialog
                    //
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(LibraryActivity.this);
                    builder1.setView(inflatedView);
                    builder1.setPositiveButton(getText(R.string.ok), (dialogInterface1, i1) -> {

                        String email = ((EditText) inflatedView
                                .findViewById(R.id.dialog_layout_set_email_et_email))
                                .getText().toString();

                        String password = ((EditText) inflatedView
                                .findViewById(R.id.dialog_layout_set_email_et_password))
                                .getText().toString();

                        OnCompleteListener<AuthResult> onCompleteListener = task -> {

                            if (task.isSuccessful()) {

                                new Thread(() -> {

                                    try {

                                        // TODO: e postayı veritabanına kaydet

                                        //firebaseUser = firebaseAuth.getCurrentUser();
                                        /*
                                        haldun.update.user.email(CurrentUser.user.getId(), firebaseUser.getEmail());
                                        haldun.update.user.uid(CurrentUser.user.getId(), firebaseUser.getUid());
                                        haldun.update.user.password(CurrentUser.user.getId(), Cryptor.encryptString(password));


                                         */
                                    } catch (Exception e) {
                                        // TODO: start error activity
                                    }

                                    throw new RuntimeException("Not ready yet");

                                }).start();

                                Toast.makeText(LibraryActivity.this, "E posta başarıyla eklendi", Toast.LENGTH_SHORT).show();

                            }else {
                                Toast.makeText(LibraryActivity.this, "Hata!", Toast.LENGTH_SHORT).show();
                            }

                        };

                        firebaseUserService.createUser(email, password, onCompleteListener);

                    });

                    AlertDialog dialog = builder1.create();
                    dialog.show();

                })
                .setNegativeButton("İptal", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout() {

        Tools.Preferences preferencesTool = new Tools.Preferences(getSharedPreferences(
                Tools.Preferences.NAME, MODE_PRIVATE));

        preferencesTool.setValue(Tools.Preferences.Keys.REMEMBER_ME, false);

        //firebaseAuth.signOut();
        firebaseUserService.signOut();

        CurrentUser.user = null;

        startActivity(new Intent(LibraryActivity.this, WelcomeActivity.class));
        finish();
    }

    Book[] filterBooks(Book[] originalList, String query) {

        ArrayList<Book> filteredBooksList = new ArrayList<>();

        for (Book book : originalList) {
            if (book.getName().toLowerCase().contains(query)) {
                filteredBooksList.add(book);
            }
        }

        //return filteredBooksList.toArray(new Book[filteredBooksList.size()]);
        return filteredBooksList.toArray(new Book[0]);

    }

    private void init() {

        //firebaseAuth = FirebaseAuth.getInstance();
        //firebaseUser = firebaseAuth.getCurrentUser();
        firebaseUserService = new FirebaseUserService();

        recyclerView = findViewById(R.id.activity_library_recyclerview);
        progressBar = findViewById(R.id.activity_library_progressbar);
        actionbar = findViewById(R.id.activity_library_actionbar);
        fab_addBook = findViewById(R.id.activity_library_fab_addBook);
        relativeLayout = findViewById(R.id.activity_library_relative_layout);
        swipeRefreshLayout = findViewById(R.id.activity_library_swipe_refresh_layout);

        databaseManager = new Manager(new MariaDB());

        //
        // Recycler View
        //
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_fall_down);
        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);

        bookAdapter = new BookAdapter(this, new ArrayList<Book>().toArray(new Book[0]));

        recyclerView.setLayoutAnimation(layoutAnimationController);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookAdapter);

        progressBar.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(false);

        bookLoader = new BookLoader(getApplicationContext(), bookAdapter);

        preferencesTool = new Preferences(getSharedPreferences(Preferences.NAME, MODE_PRIVATE));

        // Get sorting
        this.lastSetSorting = Sorting.valueOf(preferencesTool.getInt(Preferences.Keys.BOOK_SORTING_TYPE));

        positionChangeListener = (position) -> {

            Log.i("BookAdapter", "position: " + position);

            if ((position % BookLoader.RANGE == 0 && position >= lastMaxPos - 10) || position == bookAdapter.getItemCount() - 2) {
                bookLoader.resume();
            }

            if (position > lastMaxPos) {

                Log.i("BookAdapter", "last max position: " + position);
                lastMaxPos = position;

            }

            if (position < lastMaxPos) {
                Log.e("HaldununLogu", "Şu anda maks gittiğiniz indexten aşağıdasınız. Last max index: " + lastMaxPos);
            }
        };
    }
}