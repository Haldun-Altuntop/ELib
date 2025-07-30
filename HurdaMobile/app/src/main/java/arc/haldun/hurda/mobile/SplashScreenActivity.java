package arc.haldun.hurda.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.UnknownHostException;
import java.util.Objects;

import arc.haldun.hurda.api.ScrapBridge;
import arc.haldun.hurda.api.SessionIdHolder;
import arc.haldun.hurda.mobile.noconnection.NoConnectionActivity;

@SuppressWarnings("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvProgress;

    private Thread thread;

    private boolean hasSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activty_splash_screen_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        startProgress();

    }

    private void startProgress() {
        thread = new Thread(() -> {
            loadPreferences();
            checkUpdates();
            checkSession();
            startApp();
        });

        thread.start();
    }

    private void loadPreferences() {
        // TODO: load preferences
        setProgress(20, "Tercihler yükleniyor");
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private void checkUpdates() {
        // TODO: check updates
        setProgress(40, "Güncellemeler kontrol ediliyor");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        setProgress(60, "Güncellemeler kontrol ediliyor");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private void checkSession() {
        setProgress(80, "Oturum kontrol ediliyor");

        SessionIdHolder.initFileForAndroid(getApplicationContext());

        try {
            hasSession = ScrapBridge.hasSession(); // fixme: internet olmadığında hata fırlatıyor
        } catch (RuntimeException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("UnknownHost")) {
                startActivity(new Intent(
                        SplashScreenActivity.this,
                        NoConnectionActivity.class
                ));
                finish();
                try {

                    synchronized (thread) {
                        thread.wait();
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                thread.interrupt();
            }
        }
    }

    private void startApp() {
        setProgress(100, "Uygulama başlatılıyor");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }

        if (hasSession) {
            startActivity(new Intent(this, HomePageActivity.class));
        } else {
            SessionIdHolder.clearSessionId();
            startActivity(new Intent(this, WelcomeActivity.class));
        }

        finish();
    }

    private void initViews() {
        progressBar = findViewById(R.id.activity_splash_screen_progressbar);
        tvProgress = findViewById(R.id.activity_splash_screen_tv_progress_label);
    }

    private void setProgress(int percentage, String text) {
        runOnUiThread(() -> {
            progressBar.setProgress(percentage);
            tvProgress.setText(text);
        });
    }
}