package arc.haldun.mylibrary.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.Request;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.adapters.RequestAdapter;
import arc.haldun.mylibrary.server.api.ELibUtilities;

public class RequestsActivity extends AppCompatActivity {

    private Toolbar actionbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Manager databaseManager;
    private final ArrayList<Request> requests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        setSupportActionBar(actionbar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle("Talepler");
            supportActionbar.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        Animation animation = AnimationUtils.loadAnimation(
                getApplicationContext(),
                R.anim.item_animation_fall_down
        );
        LayoutAnimationController layoutAnimationController =
                new LayoutAnimationController(animation);

        new Thread(() -> {

            //databaseManager.selectRequest(this::onRequestProcess);
            /*
            RequestAdapter requestAdapter = new RequestAdapter(
                    new ArrayList<>(
                            Arrays.asList()
                    )
            );
             */

            JSONObject requestsJson = ELibUtilities.getPendingRequests();

            for (int i = 0; i < requestsJson.length(); i++) {
                JSONObject requestJson = requestsJson.optJSONObject(String.valueOf(i));
                if (requestJson == null) continue;
                Request  request = new Request();
                requests.add(request);
            }

            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {

                RequestAdapter requestAdapter = new RequestAdapter(requests);
                recyclerView.setAdapter(requestAdapter);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setLayoutAnimation(layoutAnimationController);

                progressBar.setVisibility(View.GONE);
                if (requests.isEmpty()) Toast.makeText(this, "Talep listesi boş.", Toast.LENGTH_SHORT).show();
            });

        }).start();
    }

    private void init() {
        actionbar = findViewById(R.id.activity_requests_actionbar);
        recyclerView = findViewById(R.id.activity_requests_recycler_view_requests);
        progressBar = findViewById(R.id.activity_requests_progress_bar);

        databaseManager = new Manager(new MariaDB().setExceptionListener(e-> e.printStackTrace()));
    }

    private void onRequestProcess(Request request) {
        if (request.getState() == Request.State.PENDING) requests.add(request);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }
}