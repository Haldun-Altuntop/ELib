package arc.haldun.mylibrary.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.exception.OperationFailedException;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;

public class BookDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    Book currentBook;
    //String contributor;

    EditText et_id, et_bookname, et_author, et_owner;

    TextView tv_availability, tv_publisher, tv_publicationYear, tv_page, tv_type, tv_assetNumber,
            tv_regDate, tv_cabinetNumber, tv_popularity;

    Button btn_save;
    Toolbar actionbar;

    Manager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        initCurrentBook(); // Get current book
        init(); // Init views

        //
        // Init actionbar
        //
        setSupportActionBar(actionbar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(getString(R.string.book_details));
            supportActionbar.setDisplayHomeAsUpEnabled(true);
        }


        if (!CurrentUser.user.getPriority().equals(User.SUPERUSER)) { // Check superuser
            et_id.setEnabled(false);
            et_bookname.setEnabled(false);
            et_author.setEnabled(false);
            et_owner.setEnabled(false);
            btn_save.setVisibility(View.GONE);
        }

        et_id.setText(String.valueOf(currentBook.getId()));
        et_bookname.setText(currentBook.getName());
        et_author.setText(currentBook.getAuthor());
        et_owner.setText(currentBook.getContributor().getName());

        tv_publisher.append(currentBook.getPublisher());
        tv_publicationYear.append(currentBook.getPublicationYear());
        tv_page.append(String.valueOf(currentBook.getPage()));
        tv_type.append(currentBook.getType().getStringValue());
        tv_assetNumber.append(currentBook.getAssetNumber());
        tv_regDate.append(currentBook.getRegistrationDate().getDateTime());
        tv_cabinetNumber.append(String.valueOf(currentBook.getCabinetNumber()));
        tv_popularity.append(String.valueOf(currentBook.getPopularity()));


        checkBookAvailability();

        easterEggForRose();

        new Thread(()->{

            try {
                Thread.sleep(100);
                databaseManager.addBookLog(currentBook, CurrentUser.user, new DateTime());
            } catch (InterruptedException e) {
                Log.e("BookDetailsActivity", "Kitap kaydı eklenemedi. Thread hatalıydı.");
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) finish();

        return true;
    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_save)) {
            // TODO: save book info changes
        }
    }

    private void checkBookAvailability() {

        if (currentBook.getBorrowedBy() != 0) {

            new Thread(() -> {

                User user = null;
                try {
                    user = databaseManager.getUser(currentBook.getBorrowedBy());
                } catch (OperationFailedException e) {
                    throw new RuntimeException(e);
                }

                User finalUser = user;
                new Handler(Looper.getMainLooper())
                        .post(() -> tv_availability.setText(
                                String.format("Bu kitap %s tarafından okunuyor", finalUser.getName()))
                        );
            }).start();

        } else {
            tv_availability.setText("Müsait");
        }
    }

    private void easterEggForRose() {

        if (currentBook.getId() == 1 && CurrentUser.user.getId() == 33) {
            tv_availability.setText("Bu kitabı alamazsınız. Gül, bahçesinde güzel.");
            tv_availability.setTextColor(Color.RED);
        }
    }

    private void init() {
        et_id = findViewById(R.id.activity_book_details_et_bookid);
        et_bookname = findViewById(R.id.activity_book_details_et_bookname);
        et_author = findViewById(R.id.activity_book_details_et_author);
        et_owner = findViewById(R.id.activity_book_details_et_owner);

        tv_availability = findViewById(R.id.activity_book_details_tv_availability);
        tv_publisher = findViewById(R.id.activity_book_details_tv_publisher);
        tv_publicationYear = findViewById(R.id.activity_book_details_tv_publication_year);
        tv_page = findViewById(R.id.activity_book_details_tv_page);
        tv_type = findViewById(R.id.activity_book_details_tv_type);
        tv_assetNumber = findViewById(R.id.activity_book_details_tv_asset_number);
        tv_regDate = findViewById(R.id.activity_book_details_tv_registration_date);
        tv_cabinetNumber = findViewById(R.id.activity_book_details_tv_cabinet_number);
        tv_popularity = findViewById(R.id.activity_book_details_tv_popularity);

        btn_save = findViewById(R.id.activity_book_details_btn_save);
        actionbar = findViewById(R.id.activity_book_details_actionbar);


        databaseManager = new Manager(new MariaDB());
    }

    private void initCurrentBook() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            currentBook = (Book) extras.get("book");

            new Thread(() -> {

                // FIXME: 1.06.2024 -> popülerlik arttırılırken veritabaındaki değişkene doğrudan erişilmeli

                currentBook.increasePopularity();

                databaseManager.updateBook(currentBook);

            }).start();


        } else {
            Toast.makeText(this, "Kitap seçilmemiş", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}