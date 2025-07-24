package arc.haldun.hurda.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import arc.haldun.hurda.mobile.parameters.SetParametersActivity;

public class HomePageActivity extends AppCompatActivity {

    private Button btnSetParameters, btnCreateMixture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        btnSetParameters.setOnClickListener(v -> startActivity(new Intent(
                this,
                SetParametersActivity.class
        )));

        btnCreateMixture.setOnClickListener(v -> startActivity(new Intent(
                this,
                CreateMixtureActivity.class
        )));
    }

    private void initViews() {
        btnSetParameters = findViewById(R.id.activity_home_page_btn_set_parameters);
        btnCreateMixture = findViewById(R.id.activity_home_page_btn_create_mixture);
    }
}