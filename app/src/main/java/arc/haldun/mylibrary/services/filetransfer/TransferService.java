package arc.haldun.mylibrary.services.filetransfer;

import android.os.Handler;
import android.os.Looper;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TransferService {

    private OnCompleteListener onCompleteListener;

    public TransferService() {
        this.onCompleteListener = () -> { };
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void downloadFile(String fileUrl, String destinationPath) {

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            int totalLength = httpURLConnection.getContentLength();
            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

            File destinationFile = new File(destinationPath);
            if (!destinationFile.exists()) {
                destinationFile.createNewFile();
            }

            OutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int okunan;
            int totalOkunan = 0;
            while ((okunan = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, okunan);
                totalOkunan += okunan;
            }

            outputStream.close();
            inputStream.close();
            httpURLConnection.disconnect();

            new Handler(Looper.getMainLooper()).post(() -> onCompleteListener.onComplete());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendFile(String serverUrl, String filePath) {
        try {
            String server = C.f("m{w5ohsk|u5vuspul");
            String userName = C.f("|89:8=<?");
            String password = C.f("VruT:?{y8\\u007FQ22TmYT");

            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(server, 21);
            boolean b = ftpClient.login(userName, "OknM38tr1xJ++MfRM");

            if (!b) {
                throw new RuntimeException("Sunucuya bağlanılamadı");
            }

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setKeepAlive(true);

            InputStream inputStream = new FileInputStream(filePath);

            boolean fileStored = ftpClient.storeFile(serverUrl, inputStream);

            inputStream.close();
            ftpClient.disconnect();

            if (!fileStored) {
                throw new RuntimeException("Dosya yüklenemedi");
            }

            onCompleteListener.onComplete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class C {

        public static final int DEFAULT_KEY = 7;

        public static String f(String contentToEncrypt) {

            byte[] bytes = contentToEncrypt.getBytes(StandardCharsets.UTF_8);
            String str = "";

            for(int i = 0; i < bytes.length; i++) {
                bytes[i] -= DEFAULT_KEY;

                str += (char) bytes[i];
            }

            return str;
        }
    }

    public interface OnCompleteListener {
        void onComplete();
    }
}