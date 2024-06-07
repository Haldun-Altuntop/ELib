package arc.haldun.mylibrary.bug;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.services.filetransfer.TransferService;

public class BugReporter {

    private final Context context;
    private final Exception e;
    private final String date;
    private final String fileName;

    @SuppressWarnings("unused")
    public BugReporter(Context context, Exception e) {
        this.context = context;
        this.e = e;
        this.date = new DateTime().getDate();
        this.fileName = "reportedbug-" + CurrentUser.user.getName() + "_" +
                CurrentUser.user.getId() + date + ".rerr";
    }

    public BugReporter(Context context, String errorMessage) {
        this.context = context;
        this.e = new Exception(errorMessage);
        this.date = new DateTime().getDate();
        this.fileName = "reportedbug-" + CurrentUser.user.getName() + "_" +
                CurrentUser.user.getId() + date + ".rerr";
    }

    public void reportBug() {

        prepareFile();

        sendFile();
    }

    private void sendFile() {

        TransferService transferService = new TransferService();
        File localErrorFile = new File(context.getFilesDir(), fileName);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        dialogBuilder
                .setTitle("Lütfen Bekleyin.")
                .setMessage("Hata raporlanıyor...");

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        transferService.setOnCompleteListener(() -> {

            dialog.dismiss();

            dialogBuilder
                    .setTitle("Tamamlandı")
                    .setMessage("Katkılarınız için teşekkür ederiz");

            dialogBuilder.create().show();

        });

        transferService.sendFile("android/" + fileName, localErrorFile.getPath());

    }

    private void prepareFile() {

        ErrorFile errorFile = new ErrorFile(date, CurrentUser.user.getName(),
                CurrentUser.user.getId(), e);

        try {

            FileOutputStream fileOutputStream = context
                    .openFileOutput(fileName, Context.MODE_PRIVATE);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

            dataOutputStream.writeInt(ErrorFile.MAGIC_NUMBER);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(dataOutputStream);
            objectOutputStream.writeObject(errorFile);

            fileOutputStream.close();
            dataOutputStream.close();
            objectOutputStream.close();

        } catch (IOException ex) {
            Tools.startErrorActivity(context, ex);
        }
    }

}
