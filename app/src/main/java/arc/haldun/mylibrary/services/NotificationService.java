package arc.haldun.mylibrary.services;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.driver.IDatabase;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.Notification;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.server.api.ELibUtilities;
import arc.haldun.mylibrary.server.api.UnauthorizedUserException;

public class NotificationService {

    public static final String READ_NOTIFICATIONS_FILE = "read-notifications.e-lib";
    private final Activity activity;
    private OnTaskResultListener onTaskResultListener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Manager databaseManager;
    AlertDialog.Builder dialogBuilder;

    private Notification[] notifications;

    public NotificationService(Activity activity) {
        initDatabaseTools();

        this.activity = activity;
    }

    public NotificationService(Activity activity, OnTaskResultListener onTaskResultListener) {
        initDatabaseTools();

        this.activity = activity;
        this.onTaskResultListener = onTaskResultListener;
    }

    public void start() {

        Thread networkThread = new Thread(() -> {

            //notifications = databaseManager.getNotification(CurrentUser.user.getId());

            try {
                JSONObject notificationsJson = ELibUtilities.getNotifications();

                notifications = new Notification[notificationsJson.length()];

                for (int i = 0; i < notificationsJson.length(); i++) {

                    Notification notification = new Notification(notificationsJson.getJSONObject(String.valueOf(i)));
                    notifications[i] = notification;
                }

            } catch (UnauthorizedUserException | JSONException e) {
                throw new RuntimeException(e);
            }

            Runnable runnable = () -> onTaskResultListener.onTaskResult(notifications);
            handler.post(runnable);

        });
        networkThread.start();

    }

    public void showAllNotifications() {

        for (Notification notification : notifications) {

            if (!isNotificationRead(notification)) {

                Spanned spanned = Html.fromHtml(notification.getContent(), FROM_HTML_MODE_LEGACY);

                dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(notification.getTitle())
                        .setMessage(spanned)
                        .setPositiveButton(activity.getString(R.string.ok), (dialogInterface, i) -> {
                            markNotificationAsRead(notification);
                        });

                AlertDialog dialog = dialogBuilder.create();
                dialog.setOwnerActivity(activity);
                dialog.show();
            }
        }

    }

    private void markNotificationAsRead(Notification notification) {

        try {
            File readNotificationsFile = new File(activity.getFilesDir(), READ_NOTIFICATIONS_FILE);
            if (readNotificationsFile.exists()) {

                String readNotification = notification.getId() + "-";

                FileWriter fileWriter = new FileWriter(readNotificationsFile, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                bufferedWriter.write(readNotification);

                bufferedWriter.close();
                fileWriter.close();

            } else {
                readNotificationsFile.createNewFile();
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean isNotificationRead(Notification notification) {

        boolean read = false;

        try {
            File readNotificationsFile = new File(activity.getFilesDir(), READ_NOTIFICATIONS_FILE);
            if (readNotificationsFile.exists()) {

                FileReader fileReader = new FileReader(readNotificationsFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String[] readNotifications = bufferedReader.readLine().split("-");

                fileReader.close();
                bufferedReader.close();

                for (String readNotification : readNotifications) {

                    if (Integer.parseInt(readNotification) == notification.getId()) {
                        read = true;
                    }

                }

            } else {
                readNotificationsFile.createNewFile();
            }



        } catch (FileNotFoundException e) {
            try {
                activity.openFileOutput(READ_NOTIFICATIONS_FILE, Context.MODE_APPEND).close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return read;

    }

    public interface OnTaskResultListener {
        void onTaskResult(Notification[] notifications);
    }

    private void initDatabaseTools() {

        IDatabase.IExceptionListener exceptionListener = e -> {
            Log.e("NotificationService", "Bildirim alınırken bir hata meydana geldi:\n");
            e.printStackTrace();
        };

        MariaDB mariaDB = new MariaDB();
        mariaDB.setExceptionListener(exceptionListener);

        databaseManager = new Manager(mariaDB);
    }

    public void setOnTaskResultListener(OnTaskResultListener onTaskResultListener) {
        this.onTaskResultListener = onTaskResultListener;
    }

}
