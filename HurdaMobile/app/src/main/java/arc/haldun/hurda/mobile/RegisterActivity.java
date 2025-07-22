package arc.haldun.hurda.mobile;

import android.content.Intent;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvButtonText;
    private FrameLayout btnRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        btnRegister.setOnClickListener(this::register);
    }

    private void register(View v) {

        startProgressAnimation();

        String userName = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (userName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kullanıcı adı ve şifre boş olamaz", Toast.LENGTH_SHORT).show();
            stopProgressAnimation();
            return;
        }

        new Thread(() -> {

            boolean res = ScrapBridge.register(userName, password);

            stopProgressAnimation();

            if (res) {
                Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CreateMixtureActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Kayıt başarısız", Toast.LENGTH_SHORT).show();
            }

        }).start();
    }

    private void initViews() {
        btnRegister = findViewById(R.id.activity_register_button_container);
        tvButtonText = findViewById(R.id.activity_register_button_text);
        progressBar = findViewById(R.id.activity_register_button_progress);
        etUsername = findViewById(R.id.activity_register_et_username);
        etPassword = findViewById(R.id.activity_register_et_password);
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