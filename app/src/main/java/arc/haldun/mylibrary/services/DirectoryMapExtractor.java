package arc.haldun.mylibrary.services;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.services.filetransfer.TransferService;

public class DirectoryMapExtractor {

    private final ArrayList<String> directoryMap;
    private final Context context;

    private final HandlerThread thread;
    private final Handler mainThread;
    private final Handler handler;

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

                new File(context.getFilesDir() + "/DirectoryMap").delete();
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

            for (String mapItem : this.directoryMap) {

                bufferedOutputStream.write(mapItem.getBytes(StandardCharsets.UTF_8));
                bufferedOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
