package arc.haldun.mylibrary.services;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import arc.haldun.database.Sorting;
import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.listener.OnBookProcessListener;
import arc.haldun.database.objects.Book;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.adapters.BookAdapter;

public class BookLoader {

    public static final int RANGE = 100;

    public Book[] books;
    private final Manager manager;
    private HandlerThread handlerThread;
    private Handler handler;
    private final Handler mainHandler;
    private final BookAdapter bookAdapter;
    private Sorting sorting;
    private final Context context;

    OnBookProcessListener bookProcessListener;

    public BookLoader(Context context, BookAdapter bookAdapter) {

        this.handlerThread = new HandlerThread("BookLoaderThread");
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.context = context;
        this.bookAdapter = bookAdapter;
        this.sorting = Sorting.OLD_TO_NEW; // Default value

        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());

        bookProcessListener = (book, i) -> {

            Log.e("index of book", "pos: " + i);

            if (i == 0 || i % RANGE != 0) {

                mainHandler.post(() -> bookAdapter.addItem(book));

            } else {
                try {
                    synchronized (handler) {
                        handler.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        };

        manager = new Manager(new MariaDB().setOnBookProcessListener(bookProcessListener));
    }

    public void start() {

        setSorting();

        handler.post(() -> books = manager.selectBook(sorting));

    }

    public void restart() {

        //handler = new Handler(handlerThread.getLooper());

        handlerThread.quit();
        bookAdapter.reset();

        handlerThread = new HandlerThread("BookLoaderThread");
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());

        start();

    }

    private void setSorting() {

        Tools.Preferences preferencesTool = new Tools.Preferences(this.context.getSharedPreferences(Tools.Preferences.NAME, Context.MODE_PRIVATE));

        this.sorting = Sorting.valueOf(preferencesTool.getInt(Tools.Preferences.Keys.BOOK_SORTING_TYPE));
    }

    public void resume() {
        synchronized (handler) {
            handler.notifyAll();
        }
    }

}
