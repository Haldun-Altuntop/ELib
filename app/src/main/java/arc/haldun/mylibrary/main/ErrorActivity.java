package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;

import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.bug.BugReporter;

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

        runOnUiThread(() -> {
            BugReporter bugReporter = new BugReporter(getApplicationContext(), errorMessage);
            bugReporter.reportBug();
        });

        System.err.println(errorMessage);
    }

    private void restartApp(View view) {

        if (errorCode == FILE_SERVICE_ERROR) {
            try {
                openFileOutput("temporary.e-lib", MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

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