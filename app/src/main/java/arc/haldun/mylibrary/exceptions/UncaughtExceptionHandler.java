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
import java.util.Objects;

import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.main.ErrorActivity;

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

                boolean result = logFile.createNewFile();
                if (!result) throw new RuntimeException("Dosya oluşturulamadı");
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
            ex.printStackTrace(System.err);
        }
    }

    private void startNewThread(Throwable e) {

        HandlerThread handlerThread = new HandlerThread("Uncaught Exception Thread");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {

            writeExceptionToFile(e);

            if (Objects.requireNonNull(e.getMessage()).contains("java.net.SocketTimeoutException") ||
                    Objects.requireNonNull(e.getMessage()).contains("java.net.ConnectException") ||
                    Objects.requireNonNull(e.getMessage()).contains("java.net.UnknownHostException")||
                    Objects.requireNonNull(e.getMessage()).contains("java.net.SocketException")) {
                Tools.startErrorActivity(context, (Exception) e, ErrorActivity.NETWORK_ERROR);
            }
            else
                Tools.startErrorActivity(context, (Exception) e, ErrorActivity.UNKNOWN_ERROR);

        });

        Looper.loop();

    }
}
