package arc.haldun.hurda.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import arc.haldun.hurda.api.ScrapBridge;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserName, etPassword;

    private FrameLayout buttonContainer;
    private TextView tvButtonText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        buttonContainer.setOnClickListener(this::buttonClick);

    }

    private void buttonClick(View v) {
        new Thread(() -> {
            Looper.prepare();

            startProgressAnimation();

            String userName = etUserName.getText().toString();
            String password = etPassword.getText().toString();

            if (userName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                stopProgressAnimation();
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            boolean res = ScrapBridge.login(userName, password);

            stopProgressAnimation();

            if (res) {
                startActivity(new Intent(this, HomePageActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            }

        }).start();
    }

    private void initViews() {
        this.etUserName = findViewById(R.id.activity_login_et_username);
        this.etPassword = findViewById(R.id.activity_login_et_password);

        this.buttonContainer = findViewById(R.id.activity_login_button_container);
        this.tvButtonText = findViewById(R.id.activity_login_button_text);
        this.progressBar = findViewById(R.id.activity_login_button_progress);
    }

    private void startProgressAnimation() {
        runOnUiThread(() -> {
            tvButtonText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void stopProgressAnimation() {
        runOnUiThread(() -> {
            tvButtonText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        });
    }
}