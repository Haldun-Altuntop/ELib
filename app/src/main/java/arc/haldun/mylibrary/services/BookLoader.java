package arc.haldun.mylibrary.services;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import arc.haldun.database.Sorting;
import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.driver.Connector;
import arc.haldun.database.listener.OnBookProcessListener;
import arc.haldun.database.objects.Book;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.adapters.BookAdapter;
import arc.haldun.mylibrary.developer.DeveloperUtilities;
import arc.haldun.mylibrary.main.SplashScreenActivity;

public class BookLoader {

    public static final int RANGE = 100;

    private MariaDB mariaDB;
    private final Manager manager;
    private HandlerThread handlerThread;
    private Handler handler;
    private final Handler mainHandler;
    private final BookAdapter bookAdapter;
    private Sorting sorting;
    private final Context context;
    private boolean paused;

    OnBookProcessListener bookProcessListener;

    public BookLoader(Context context, BookAdapter bookAdapter) {

        this.context = context;
        this.bookAdapter = bookAdapter;
        this.sorting = Sorting.OLD_TO_NEW; // Default value
        this.mainHandler = new Handler(Looper.getMainLooper());

        this.handlerThread = new HandlerThread("BookLoaderThread");
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());

        bookProcessListener = this::onBookProcess;

        mariaDB = new MariaDB();
        mariaDB.setOnBookProcessListener(bookProcessListener);

        manager = new Manager(mariaDB);
    }

    private void onBookProcess(Book book, int i) {

        if (i == 0 || i % RANGE != 0) {

            mainHandler.post(() -> bookAdapter.addItem(book));
            //System.out.println("Kitap numarası: " + i);

        } else {

            mariaDB.pause();

            Log.i("Book Loader", "Maksimum aralığa ulaşıldı. Book Loader bekletiliyor.");
        }
    }

    public void start() {

        if (DeveloperUtilities.isOffline) {
            Log.e("BookLoader", "Çevrimdışı mod etkin olduğunda Book Loader başlatılamaz.");
            return;
        }

        setSorting();

        mariaDB.resume();

        handler.post(() -> {

            if (!Connector.isValid()) SplashScreenActivity.rConnectDatabase.run();

            manager.selectBook(sorting);
        });

    }

    public void restart() {

        if (DeveloperUtilities.isOffline) {
            Log.e("BookLoader", "Çevrimdışı mod etkin olduğunda Book Loader yeniden başlatılamaz.");
            return;
        }

        handlerThread.quit();
        bookAdapter.reset();

        handlerThread = new HandlerThread("BookLoaderThread");
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());

        start();

    }

    private void setSorting() {

        Tools.Preferences preferencesTool = new Tools.Preferences(
                this.context.getSharedPreferences(Tools.Preferences.NAME, Context.MODE_PRIVATE)
        );

        this.sorting = Sorting.valueOf(preferencesTool.getInt(Tools.Preferences.Keys.BOOK_SORTING_TYPE));
    }

    public void resume() {

        if (DeveloperUtilities.isOffline) {
            Log.e("BookLoader", "Çevrimdışı mod etkin olduğunda Book Loader devam ettirilemez.");
            return;
        }

        mariaDB.resume();

    }

    public void pause() {

        if (DeveloperUtilities.isOffline) {
            Log.e("BookLoader", "Çevrimdışı mod etkin olduğunda Book Loader bekletilemez.");
            return;
        }

        mariaDB.pause();
    }

    public void stop() {

        if (DeveloperUtilities.isOffline) {
            Log.e("BookLoader", "Çevrimdışı mod etkin olduğunda Book Loader dururulamaz.");
            return;
        }

        manager.stopCurrentOperation();
    }
}
