package arc.haldun.mylibrary.main;

import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;
import arc.haldun.mylibrary.R;

public class AddBookActivity extends AppCompatActivity {

    Toolbar actionbar;
    EditText et_bookName, et_author;
    Button btn_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        init(); // Init view

        setSupportActionBar(actionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.add_book));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btn_add.setOnClickListener(this::click);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) finish();

        return true;
    }

    private void init() {
        actionbar = findViewById(R.id.activity_add_book_actionbar);
        et_bookName = findViewById(R.id.activity_add_book_et_bookName);
        et_author = findViewById(R.id.activity_add_book_et_author);
        btn_add = findViewById(R.id.activity_add_book_btn_add);
    }

    private void click(View view) {

        String bookName = et_bookName.getText().toString();
        String author = et_author.getText().toString();

        if (bookName.isEmpty()) {
            Toast.makeText(this, getString(R.string.book_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
        } else if (author.isEmpty()) {
            Toast.makeText(this, getString(R.string.author_cannot_be_empty), Toast.LENGTH_SHORT).show();
        } else {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();

                    Book book = new Book();
                    book.setName(bookName);
                    book.setAuthor(author);
                    book.setContributor(CurrentUser.user);
                    book.setRegistrationDate(new DateTime());

                    Manager manager = new Manager(new MariaDB());
                    manager.addBook(book);
                    Toast.makeText(getApplicationContext(), getString(R.string.book_successfully_added), Toast.LENGTH_SHORT).show();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            et_bookName.setText("");
                            et_author.setText("");
                        }
                    });
                }
            }).start();

        }
    }
}