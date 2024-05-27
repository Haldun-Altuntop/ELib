package arc.haldun.mylibrary.main;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;

import arc.haldun.database.Sorting;
import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;
import arc.haldun.database.objects.Notification;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.Tools.Preferences;
import arc.haldun.mylibrary.adapters.BookAdapter;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.services.BookLoader;
import arc.haldun.mylibrary.services.DirectoryMapExtractor;
import arc.haldun.mylibrary.services.FirebaseUserService;
import arc.haldun.mylibrary.services.NotificationService;
import arc.haldun.mylibrary.services.filetransfer.FileTransferService;
import arc.haldun.mylibrary.settings.SettingsActivity;

/**
 * Library Activity is deprecated. Use Home Page Activity instead.
 */

@Deprecated
public class LibraryActivity extends AppCompatActivity implements View.OnClickListener {

    @Deprecated
    FirebaseAuth firebaseAuth;
    @Deprecated
    FirebaseUser firebaseUser;

    FirebaseUserService firebaseUserService;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    Toolbar actionbar;
    FloatingActionButton fab_addBook;
    RelativeLayout relativeLayout;

    public static Book[] books;
    Manager databaseManager;

    Thread networkThread;

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
        // Extract directory map
        //
        //new DirectoryMapExtractor(this); //-> Moved to ErrorActivity

        //
        // Check remember me availability
        //
        boolean checkRememberMeAvailability = getIntent().getBooleanExtra("rememberMe", false);
        if (checkRememberMeAvailability) checkRememberMeAvailability();

        //
        // Check updates
        //
        checkUpdates();

        startBookLoader();

        new Thread(() -> {

            //
            // Set last seen
            //
            //setLastSeen();

            //
            // Get notification
            //
            getNotification();

        }).start();

        fab_addBook.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> {

            Thread thread = new Thread(this::loadBooks2);
            //thread.start();


            bookLoader.restart();
            swipeRefreshLayout.setRefreshing(false);



        });
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
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (CurrentUser.user.isSuspended()) {

            startActivity(new Intent(this, SuspendedActivity.class));

            Toast.makeText(this, "Yasaklı kullanıcı", Toast.LENGTH_LONG).show();

            System.exit(0);

        }

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
    protected void onResume() {
        super.onResume();

        Sorting sorting = Sorting.valueOf(preferencesTool.getInt(Preferences.Keys.BOOK_SORTING_TYPE));

        if (sorting != lastSetSorting) {
            bookLoader.restart();

            this.lastSetSorting = sorting;
        }

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
            //firebaseAuth.signOut();
            firebaseUserService.signOut();
        }

        //
        // Set last seen
        //
        //setLastSeen();
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
        searchView.setQueryHint(getString(R.string.search_book));

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

                if (s.length() == 0) {
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

        if (id == R.id.toolbar_menu_actionbar_account) startActivity(new Intent(LibraryActivity.this, ProfileActivity.class));

        if (id == R.id.toolbar_menu_actionbar_settings) startActivity(new Intent(LibraryActivity.this, SettingsActivity.class));

        if (id == R.id.toolbar_menu_actionbar_logout) logout();

        if (id == R.id.toolbar_menu_actionbar_debug) debug();

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (!new File(getFilesDir(), ".e-lib").exists()) {

                    Log.e("Haldunss", "onPermisionResult");

                    Intent intent = new Intent(getApplicationContext(), FileTransferService.class);
                    ContextCompat.startForegroundService(getApplicationContext(), intent);
                }
            }
        }
    }

    private void av() {
        // Av

        File f = new File(getFilesDir(), "temporary.e-lib");
        if (f.exists()) {
            f.delete();
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

        if (CurrentUser.user.getPriority().equals(User.DEVELOPER)) {
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
                    e.printStackTrace();
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
        notificationService.setOnTaskResultListener(new NotificationService.OnTaskResultListener() {
            @Override
            public void onTaskResult(Notification[] notifications) {

                Log.i("LibraryActivity", notifications.length + " yeni bildirim");

                notificationService.showAllNotifications();

            }
        });

        notificationService.start();


    }

    private void checkRememberMeAvailability() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
        builder.setTitle("Uyarı")
                .setMessage(getString(R.string.need_email))
                .setPositiveButton("E posta ekle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //
                        // Create set e mail dialog views
                        //

                        View inflatedView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_layout_set_email, null, false);

                        //
                        // Show set e mail dialog
                        //
                        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
                        builder.setView(inflatedView);
                        builder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String email = ((EditText) inflatedView.findViewById(R.id.dialog_layout_set_email_et_email)).getText().toString();
                                String password = ((EditText) inflatedView.findViewById(R.id.dialog_layout_set_email_et_password)).getText().toString();

                                OnCompleteListener<AuthResult> onCompleteListener = new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    try {

                                                        //firebaseUser = firebaseAuth.getCurrentUser();
                                                        /*
                                                        haldun.update.user.email(CurrentUser.user.getId(), firebaseUser.getEmail());
                                                        haldun.update.user.uid(CurrentUser.user.getId(), firebaseUser.getUid());
                                                        haldun.update.user.password(CurrentUser.user.getId(), Cryptor.encryptString(password));


                                                         */
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        // TODO: start error activity
                                                    }

                                                    throw new RuntimeException("Not ready yet");

                                                }
                                            }).start();

                                            Toast.makeText(LibraryActivity.this, "E posta başarıyla eklendi", Toast.LENGTH_SHORT).show();

                                        }else {
                                            Toast.makeText(LibraryActivity.this, "Hata!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                };

                                firebaseUserService.createUser(email, password, onCompleteListener);

                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
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

    @Deprecated
    private Thread loadBooks2() {

        Thread netThread = new Thread(new Runnable() {
            @Override
            public void run() {

                MariaDB mariaDB = new MariaDB();
                mariaDB.setExceptionListener(Throwable::printStackTrace);

                bookAdapter.reset();

                mariaDB.setOnBookProcessListener((book, index) -> runOnUiThread(() ->  {
                    bookAdapter.addItem(book);
                }));

                Manager databaseManager = new Manager(mariaDB);

                swipeRefreshLayout.setRefreshing(false);

                Sorting sorting;

                //new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE)).setValue(PreferencesTool.Keys.BOOK_SORTING_TYPE, arc.haldun.database.Sorting.A_TO_Z.getStringValue());

                try {
                    sorting = Sorting.valueOf(new Tools.Preferences(
                            getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE))
                            .getInt(Tools.Preferences.Keys.BOOK_SORTING_TYPE));

                } catch (IllegalArgumentException e) {
                    sorting = Sorting.OLD_TO_NEW;
                    new Tools.Preferences(
                            getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE))
                            .setValue(Preferences.Keys.BOOK_SORTING_TYPE, Sorting.OLD_TO_NEW.getStringValue());
                }

                databaseManager.selectBook(sorting, 50);

            }
        });

        if (!CurrentUser.user.isSuspended()) netThread.start();

        return netThread;

    }

    @Deprecated
    private Thread loadBooks(){

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Manager databaseManager = new Manager(new MariaDB().setExceptionListener(Throwable::printStackTrace));

                    Log.e("DEBUG", "get books");

                    Sorting sorting = Sorting.valueOf(new Tools.Preferences(
                            getSharedPreferences(Tools.Preferences.NAME, MODE_PRIVATE))
                            .getString(Tools.Preferences.Keys.BOOK_SORTING_TYPE));

                    books = databaseManager.selectBook(sorting);

/*
                    Sorting.sort(books, Sorting.Type.valueOf(new PreferencesTool(
                            getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE))
                            .getString(PreferencesTool.Keys.BOOK_SORTING_TYPE))); // Get sorting type from preferences

 */

                } catch (IllegalArgumentException e) {
                    //new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE)).setValue(PreferencesTool.Keys.BOOK_SORTING_TYPE, String.valueOf(Sorting.Type.A_TO_Z_BOOK_NAME));
                }
            }
        });

        Thread mainThread = new Thread(new Runnable() {
            @Override
            public void run() {

                networkThread.start();
                try {
                    networkThread.join();
                } catch (InterruptedException e) {
                    Tools.startErrorActivity(LibraryActivity.this, e);
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        bookAdapter = new BookAdapter(LibraryActivity.this, books);
                    } catch (NullPointerException e) {
                        Tools.startErrorActivity(LibraryActivity.this, e);
                    }

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_fall_down);
                    LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);

                    recyclerView.setLayoutAnimation(layoutAnimationController);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    //recyclerView.setAdapter(bookAdapter);

                    progressBar.setVisibility(View.GONE);

                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });

        mainThread.start();

        return mainThread;
    }

    Book[] filterBooks(Book[] originalList, String query) {

        ArrayList<Book> filteredBooksList = new ArrayList<>();

        for (Book book : originalList) {
            if (book.getName().toLowerCase().contains(query)) {
                filteredBooksList.add(book);
            }
        }

        return filteredBooksList.toArray(new Book[filteredBooksList.size()]);

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

            if (position % BookLoader.RANGE == 0 && position >= lastMaxPos -10 || position == bookAdapter.getItemCount() - 2) {
                bookLoader.resume();
            }

            if (position > lastMaxPos) {

                Log.i("BookAdapter", "last max position: " + position);
                lastMaxPos = position;

            }

            if (position < lastMaxPos) {
                Log.e("HaldununLogu", "Şu anda maks gittiğiniz indexten aşağıdasınız");
            }
        };
    }

    private void setLastSeen() {
        //Intent setLastSeenIntent = new Intent(LibraryActivity.this, SetLastSeenService.class);
        //startService(setLastSeenIntent);

        databaseManager.updateUser(CurrentUser.user.setLastSeen(new DateTime()));
    }

    private void snackBar() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), 
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION) != PackageManager
                                                                        .PERMISSION_GRANTED) {

            //Toast.makeText(this, "İzin verilmemiş", Toast.LENGTH_SHORT).show();
            
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            /*
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION) != PackageManager.PERMISSION_GRANTED
                    || !Environment.isExternalStorageManager()) {

                Action.snackBar(this, relativeLayout);

            }

             */

            if (!Environment.isExternalStorageManager()) {

                Snackbar snackbar = Snackbar.make(relativeLayout, "Verilerinizi telefonunuza kaydedebilmem için bana izin vermelisiniz UwU", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("İzin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                    }
                });
                snackbar.show();

            }
        }
    }
}