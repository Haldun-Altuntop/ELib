package arc.haldun.mylibrary.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.exception.OperationFailedException;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;
import arc.haldun.database.objects.Request;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;

public class BookDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    Book currentBook;
    //String contributor;

    TextView tv_bookname, tv_author, tv_publicationYear, tv_page, tv_type, tv_cabinetNumber,
            tv_lbl_reserve;

    CardView cardOtherInformation, cardReserve;

    Button btn_reserve;

    ProgressBar progressBar;

    Toolbar actionbar;

    Manager databaseManager;

    boolean requested = false;

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


        tv_bookname.setText(currentBook.getName());
        tv_author.setText(currentBook.getAuthor());

        tv_publicationYear.append(currentBook.getPublicationYear());
        tv_page.append(String.valueOf(currentBook.getPage()));
        tv_type.append(currentBook.getType().getStringValue());
        tv_cabinetNumber.append(String.valueOf(currentBook.getCabinetNumber()));

        cardOtherInformation.setOnClickListener(this);
        //cardReserve.setOnClickListener(this);
        btn_reserve.setOnClickListener(this);

        checkBookAvailability();

        easterEggForRose();

        addLog();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) finish();

        return true;
    }

    @Override
    public void onClick(View view) {

        if (view.equals(cardOtherInformation)) {

        }

        if (view.equals(btn_reserve)) {
            reserveThisBook();
        }
    }

    private void reserveThisBook() {

        if (currentBook.isBorrowed() || requested) return;

        progressBar.setVisibility(View.VISIBLE);
        //tv_lbl_reserve.setVisibility(View.GONE);
        btn_reserve.setText("");

        new Thread(() -> {

            Request request = Request.createNewRequest(currentBook, CurrentUser.user);
            databaseManager.addRequest(request);

            new Handler(getMainLooper()).post(() -> {
                progressBar.setVisibility(View.GONE);
                //tv_lbl_reserve.setVisibility(View.VISIBLE);
                btn_reserve.setText("Talebiniz Alındı");
                requested = true;

                showReservationDialog();
            });

        }).start();
    }

    private void showReservationDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Talebiniz alındı")
                .setMessage("Üç gün içinde kütüphane görevlisine müracaat ediniz.")
                .setPositiveButton("Kapat", null);

        alertDialogBuilder.create().show();

    }

    private void addLog() {

        new Thread(()->{

            try {
                Thread.sleep(100);
                databaseManager.addBookLog(currentBook, CurrentUser.user, new DateTime());
            } catch (InterruptedException e) {
                Log.e("BookDetailsActivity", "Kitap kaydı eklenemedi. Thread hatalıydı.");
            }
        }).start();
    }

    private void checkBookAvailability() {

        if (currentBook.getBorrowedBy() != 0) {

            tv_lbl_reserve.setText("Bu kitap kullanımda");

            new Thread(() -> {

                User user = null;
                try {
                    user = databaseManager.getUser(currentBook.getBorrowedBy());
                } catch (OperationFailedException e) {
                    throw new RuntimeException(e);
                }

                User finalUser = user;
                /*
                new Handler(Looper.getMainLooper())
                        .post(() -> tv_availability.setText(
                                String.format("Bu kitap %s tarafından okunuyor", finalUser.getName())
                        ));
                 */
            }).start();

        } else {
            //tv_availability.setText("Müsait");
        }
    }

    private void easterEggForRose() {

        /*
        if (currentBook.getId() == 1 && CurrentUser.user.getId() == 33) {
            tv_availability.setText("Bu kitabı alamazsınız. Gül, bahçesinde güzel.");
            tv_availability.setTextColor(Color.RED);
        }
         */
    }

    private void init() {

        tv_bookname = findViewById(R.id.activity_book_details_tv_bookname);
        tv_author = findViewById(R.id.activity_book_details_tv_author);
        tv_publicationYear = findViewById(R.id.activity_book_details_tv_publication_year);
        tv_page = findViewById(R.id.activity_book_details_tv_page);
        tv_type = findViewById(R.id.activity_book_details_tv_type);
        tv_cabinetNumber = findViewById(R.id.activity_book_details_tv_cabinet_number);
        //tv_lbl_reserve = findViewById(R.id.activity_book_details_tv_lbl_reserve);

        btn_reserve = findViewById(R.id.activity_book_details_btn_reserve);

        progressBar = findViewById(R.id.activity_book_details_progress_bar);

        actionbar = findViewById(R.id.activity_book_details_actionbar);

        cardOtherInformation = findViewById(R.id.activity_book_details_card_other_information);
        //cardReserve = findViewById(R.id.activity_book_details_card_reserve);

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