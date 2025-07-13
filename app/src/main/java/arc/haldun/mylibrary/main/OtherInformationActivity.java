package arc.haldun.mylibrary.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import arc.haldun.database.objects.Book;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;

public class OtherInformationActivity
        extends AppCompatActivity
        implements View.OnClickListener {

    private Book book;

    private RelativeLayout relativeLayout;
    private TextView tv_id, tv_publisher, tv_assetNumber, tv_popularity, tv_owner, tv_regDate,
            tv_bookName;
    private Button btn_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_other_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        initBook();

        tv_bookName.setText(book.getName());
        tv_id.setText(String.valueOf(book.getId()));
        tv_publisher.setText(book.getPublisher());
        tv_assetNumber.setText(book.getAssetNumber());
        tv_popularity.setText(String.valueOf(book.getPopularity()));
        tv_owner.setText(book.getContributor().getName());
        tv_regDate.setText(book.getRegistrationDate().toString());

        btn_edit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_edit)) {
            // TODO: start edit activity
            Tools.makeText(getApplicationContext(), getString(R.string.not_supported_yet));
        }
    }

    private void initBook() {

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            try {

                String bookStr = extras.getString("bookJsonString");

                if (bookStr != null) {
                    JSONObject bookJson = new JSONObject(bookStr);
                    book = new Book(bookJson);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void init() {

        relativeLayout = findViewById(R.id.main);

        tv_bookName = findViewById(R.id.action_other_information_tv_book_name);
        tv_id = findViewById(R.id.activity_other_information_tv_id);
        tv_publisher = findViewById(R.id.activity_other_information_tv_publisher);
        tv_assetNumber = findViewById(R.id.activity_other_information_tv_asset_number);
        tv_popularity = findViewById(R.id.activity_other_information_tv_popularity);
        tv_owner = findViewById(R.id.activity_other_information_tv_owner);
        tv_regDate = findViewById(R.id.activity_other_information_tv_reg_date);

        btn_edit = findViewById(R.id.activity_other_information_btn_edit);
    }
}