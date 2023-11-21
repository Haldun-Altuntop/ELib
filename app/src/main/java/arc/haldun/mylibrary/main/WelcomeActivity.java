package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import arc.haldun.mylibrary.R;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_login, btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        init(); // Init views

        btn_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);
    }

    private void init() {
        btn_login = findViewById(R.id.welcome_activity_btn_login);
        btn_register = findViewById(R.id.welcome_activity_btn_createAccount);
    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_login)) {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (view.equals(btn_register)) {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }
}