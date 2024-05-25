package arc.haldun.mylibrary.exceptions;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arc.haldun.mylibrary.Tools;

;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public UncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        
        startNewThread(e);

    }

    private void writeExceptionToFile(Throwable e) {
        try {
            File logFile = new File(context.getExternalFilesDir(""),"Log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());

            PrintWriter printWriter = new PrintWriter(logFile);
            printWriter.println("Timestamp: " + timestamp);
            printWriter.println("Exception Details:");
            e.printStackTrace(printWriter);
            printWriter.println();
            printWriter.flush();
            printWriter.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void startNewThread(Throwable e) {

        HandlerThread handlerThread = new HandlerThread("Uncaught Exception Thread");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {

            writeExceptionToFile(e);

            Tools.startErrorActivity(context, (Exception) e);

        });

        Looper.loop();

    }

    private void launchErrorActivity() {

    }
}
