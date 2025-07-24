package arc.haldun.hurda.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import arc.haldun.hurda.api.ScrapBridge;
import arc.haldun.hurda.database.OperationFailedException;
import arc.haldun.hurda.database.objects.Scrap;

public class CreateMixtureActivity extends AppCompatActivity {

    private Toolbar actionBar;

    private RecyclerView recyclerView;
    private ScrapAdapter scrapAdapter;

    private TextView tvCalculatedEnergy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_mixture);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_home_page_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();

        initActionbar();

        ScrapHolderManager.onEnergyCalculatedListener = energy -> tvCalculatedEnergy.setText(String.valueOf(energy));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_actionbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_bar_item_logout) {
            new Thread(() -> {
                ScrapBridge.logout();

                runOnUiThread(() -> {
                    startActivity(new Intent(this, WelcomeActivity.class));
                    finish();
                });

            }).start();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initActionbar() {

        setSupportActionBar(actionBar);

        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(getString(R.string.app_name));
            supportActionbar.setDisplayShowTitleEnabled(true);
        }

    }

    private void initViews () {
        actionBar = findViewById(R.id.activity_home_page_actionbar);

        recyclerView = findViewById(R.id.activity_home_page_recycler_view);
        new Thread(() -> {
            Looper.prepare();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                List<Scrap> scraps = Arrays.asList(ScrapBridge.getAllScraps());
                scrapAdapter = new ScrapAdapter(scraps);
            } catch (OperationFailedException e) {
                e.printStackTrace(System.err);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            runOnUiThread(this::initRecyclerView);
        }).start();

        tvCalculatedEnergy = findViewById(R.id.activity_home_page_tv_calculated_energy);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(scrapAdapter);
    }
}