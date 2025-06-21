package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.bug.BugReporter;
import arc.haldun.mylibrary.server.api.ELibUtilities;

public class ErrorActivity extends AppCompatActivity {

    public static final int UNKNOWN_ERROR = 0;
    public static final int FILE_SERVICE_ERROR = 1;

    String errorMessage;
    int errorCode;

    Button btn_report, btn_restart;
    TextView tv_errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        init();

        System.err.println(errorMessage);

        tv_errorMessage.setText(errorMessage);
        btn_report.setOnClickListener(this::reportError);
        btn_restart.setOnClickListener(this::restartApp);
    }

    private void reportError(View view) {

        Log.e("ErrorActivity", errorMessage);

        new Thread(() -> {
            ELibUtilities.addError(errorMessage, "");

            Looper.prepare();

            // FIXME: 21.03.2025 -> Diyalog ile iyileştir. Kullanıcıdan ilave mesaj alınacak.
            Toast.makeText(this, "Geri ildiriminiz için teşekkür ederiz.", Toast.LENGTH_SHORT).show();
        }).start();

        btn_report.setEnabled(false);

    }

    private void restartApp(View view) {

        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Runtime.getRuntime().exit(0);
    }

    void init() {

        btn_report = findViewById(R.id.error_activity_btn_report);
        btn_restart = findViewById(R.id.error_activity_btn_restart_app);
        tv_errorMessage = findViewById(R.id.error_activity_tv_errorMessage);

        errorMessage = getIntent().getStringExtra("ErrorMessage");
        errorCode = getIntent().getIntExtra("ErrorCode", 0);
    }
}