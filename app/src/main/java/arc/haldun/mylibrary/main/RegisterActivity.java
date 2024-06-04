package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.DateTime;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    static String PREFS_NAME = "preferences";
    int minPasswordLength = 6;

    Toolbar actionbar;
    EditText et_email, et_username, et_password, et_passwordAgain;
    Button btn_register;
    CheckBox cb_rememberMe;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regsiter);
        init(); // Init views
        setAppBar(); // set app bar

        et_email.setHint(getString(R.string.prompt_email));
        et_username.setHint(getString(R.string.user_name));
        et_password.setHint(getString(R.string.password));
        et_passwordAgain.setHint(getString(R.string.password));

        btn_register.setOnClickListener(this);
        cb_rememberMe.setOnCheckedChangeListener(this);
    }

    private void init() {
        actionbar = findViewById(R.id.register_activity_actionbar);

        et_email = findViewById(R.id.register_activity_et_email);
        et_username = findViewById(R.id.register_activity_username);
        et_password = findViewById(R.id.register_activity_et_password);
        et_passwordAgain = findViewById(R.id.register_activity_et_password_again);

        btn_register = findViewById(R.id.register_activity_btn_login);

        cb_rememberMe = findViewById(R.id.register_activity_cb_rememberMe);

        progressBar = findViewById(R.id.register_activity_progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setAppBar() {
        setSupportActionBar(actionbar);

        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.register));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_register)) { // register butonuna tıklandığında

            String email = et_email.getText().toString();
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();
            String password2 = et_passwordAgain.getText().toString();

            if (email.isEmpty() || password.isEmpty()) { // eposta veya şifre boşsa

                Toast.makeText(this, getString(R.string.email_or_password_cannot_be_empty), Toast.LENGTH_SHORT).show();

            } else if (password2.isEmpty()) { // ikinci şifre boşsa

                Toast.makeText(this, getString(R.string.verify_password), Toast.LENGTH_SHORT).show();

            } else { // bilgiler tamsa

                if (password.length() < minPasswordLength) Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();

                else register(username, email, password);

                if (password.equals(password2)) { // şifreler uyuşuyorsa

                } else { // şifreler uyuşmuyorsa
                    Toast.makeText(this, getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (compoundButton.equals(cb_rememberMe)) {

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Tools.Preferences preferencesTool = new Tools.Preferences(sharedPreferences);
            preferencesTool.setValue(Tools.Preferences.Keys.REMEMBER_ME, b);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void register(String username, String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            User user = new User(firebaseAuth.getUid(), username, password, email, String.valueOf(BuildConfig.VERSION_CODE), User.USER,
                                    new DateTime(), new DateTime(), 0);

                            Manager manager = new Manager(new MariaDB());
                            manager.addUser(user);
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    Intent intent = new Intent(RegisterActivity.this, LibraryActivity.class);
                    startActivity(intent);

                    finish();
                }
            }
        });
    }
}