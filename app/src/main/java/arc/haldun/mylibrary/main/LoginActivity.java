package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ThemeUtils;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    EditText et_usernameOrEMail, et_password;
    Button btn_login;
    ProgressBar progressBar;
    CheckBox cb_rememberMe;
    Toolbar actionbar;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    Manager databaseManager;

    Tools.Preferences preferencesTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init(); // Declare contents

        setSupportActionBar(actionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        cb_rememberMe.setOnCheckedChangeListener(this);
        btn_login.setOnClickListener(this);

        et_password.setHint(getString(R.string.password));
    }

    private void init() {

        et_usernameOrEMail = findViewById(R.id.login_activity_et_email_or_username);
        et_password = findViewById(R.id.login_activity_et_password);
        btn_login = findViewById(R.id.login_activity_btn_login);
        progressBar = findViewById(R.id.login_activity_progressBar);
        cb_rememberMe = findViewById(R.id.login_activity_cb_rememberMe);
        actionbar = findViewById(R.id.login_activity_actionbar);

        firebaseAuth = FirebaseAuth.getInstance();

        preferencesTool = new Tools.Preferences(getSharedPreferences(
                Tools.Preferences.NAME, MODE_PRIVATE));

        databaseManager = new Manager(new MariaDB());

    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_login)) {

            String usernameOrEMail = et_usernameOrEMail.getText().toString();
            String password = et_password.getText().toString();

            if (!usernameOrEMail.isEmpty() && !password.isEmpty()) {

                startButtonActionAnimation();

                login(usernameOrEMail, password);

            } else {
                Toast.makeText(this, getString(R.string.email_or_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    private void login(String username, String password) {

        if (username.contains("@")){
            firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        firebaseUser = firebaseAuth.getCurrentUser();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CurrentUser.user = new MariaDB().getUser(firebaseUser.getUid());
                            }
                        });
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        startActivity(new Intent(LoginActivity.this, LibraryActivity.class));
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed_check_your_info), Toast.LENGTH_SHORT).show();
                    }

                    stopButtonActionAnimation();
                }
            });
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();

                    try {
                        User user = new User();

                        user.setName(username);
                        user.setPassword(password);

                        //user.encrypt();

                        Manager databaseManager = new Manager(new MariaDB());

                        if (databaseManager.login(user)) {

                            String email = CurrentUser.user.getEMail();
                            String password = CurrentUser.user.getPassword();

                            if (email != null && !email.isEmpty()) {
                                firebaseAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {

                                            stopButtonActionAnimation();

                                            startActivity(new Intent(LoginActivity.this,
                                                    HomePageActivity.class));
                                            finish();
                                });
                            } else {

                                Intent libraryIntent = new Intent(
                                        LoginActivity.this,
                                        HomePageActivity.class);

                                libraryIntent.putExtra("rememberMe",
                                        preferencesTool.getBoolean(
                                                Tools.Preferences.Keys.REMEMBER_ME));

                                stopButtonActionAnimation();
                                startActivity(libraryIntent);
                                finish();
                            }
                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    getString(R.string.login_failed_check_your_info),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        preferencesTool.setValue(Tools.Preferences.Keys.REMEMBER_ME, b);
    }

    private void startButtonActionAnimation() {
        progressBar.setVisibility(View.VISIBLE);
        btn_login.setText("");
    }
    private void stopButtonActionAnimation() {
        progressBar.setVisibility(View.INVISIBLE);
        btn_login.setText(getText(R.string.login));
    }
}