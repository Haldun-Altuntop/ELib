package arc.haldun.hurda.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.util.Arrays;
import java.util.List;

import arc.haldun.hurda.api.ScrapBridge;
import arc.haldun.hurda.database.OperationFailedException;
import arc.haldun.hurda.database.objects.Calculation;
import arc.haldun.hurda.database.objects.GeneralParameter;
import arc.haldun.hurda.database.objects.Mixture;
import arc.haldun.hurda.database.objects.Scrap;

public class CreateMixtureActivity extends AppCompatActivity {

    private Toolbar actionBar;

    private RecyclerView recyclerView;
    private ScrapAdapter scrapAdapter;

    private TextView tvCalculatedEnergy;

    private Button btnCalculate;

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
        btnCalculate.setOnClickListener(this::btnCalculateClick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ScrapHolderManager.clear();
    }

    private void btnCalculateClick(View v) {

        new Thread(() -> {

            Mixture mixture = ScrapHolderManager.getMixture();

            runOnUiThread(() -> Utilities.showLoadingDialog(CreateMixtureActivity.this, "Hesaplanıyor..."));

            //Calculation calculation = ScrapBridge.calculateMixture(mixture);
            // Get parameters
            try {
                GeneralParameter tapTemp = ScrapBridge.getGeneralParameter("Tap Temp [°C]");
                GeneralParameter limeAddition_uCons = ScrapBridge.getGeneralParameter("Lime Addition [kg/t] - U. Cons.");

                GeneralParameter limeCaO = ScrapBridge.getGeneralParameter("Lime CaO");
                GeneralParameter doloCaO = ScrapBridge.getGeneralParameter("Dolo CaO");
                GeneralParameter limeMgO = ScrapBridge.getGeneralParameter("Lime MgO");
                GeneralParameter doloMgO = ScrapBridge.getGeneralParameter("Dolo MgO");
                GeneralParameter limeAl2O3 = ScrapBridge.getGeneralParameter("Lime Al2O3");
                GeneralParameter doloAl2O3 = ScrapBridge.getGeneralParameter("Dolo Al2O3");
                GeneralParameter limeSiO2 = ScrapBridge.getGeneralParameter("Lime SiO2");
                GeneralParameter doloSiO2 = ScrapBridge.getGeneralParameter("Dolo SiO2");

                GeneralParameter chargeMgO = ScrapBridge.getGeneralParameter("Charge MgO");
                GeneralParameter injectionMgO = ScrapBridge.getGeneralParameter("Injection MgO");
                GeneralParameter chargeAl2O3 = ScrapBridge.getGeneralParameter("Charge Al2O3");
                GeneralParameter injectionAl2O3 = ScrapBridge.getGeneralParameter("Injection Al2O3");
                GeneralParameter chargeSiO2 = ScrapBridge.getGeneralParameter("Charge SiO2");
                GeneralParameter injectionSiO2 = ScrapBridge.getGeneralParameter("Injection SiO2");
                GeneralParameter chargeCaO = ScrapBridge.getGeneralParameter("Charge CaO");
                GeneralParameter injectionCaO = ScrapBridge.getGeneralParameter("Injection CaO");

                GeneralParameter dololimeAddition_uCons = ScrapBridge.getGeneralParameter("Dololime Addition [kg/t] - U. Cons. ");
                GeneralParameter chargeCoke_uCons = ScrapBridge.getGeneralParameter("Charge Coke [kg] - U. Cons. ");
                GeneralParameter injectionCoke_uCons = ScrapBridge.getGeneralParameter("Injection Coke [kg] - U. Cons. ");
                GeneralParameter FeOSlag = ScrapBridge.getGeneralParameter("FeO Slag [%]");

                double[] dH = {0};
                double[] slag = {0};
                double[] yield = {0};

                mixture.getScrapDoubleMap().forEach((scrap, percentage) -> {
                    slag[0] += ((
                            (scrap.getP15_SiO2() + scrap.getP14_Al2O3() + scrap.getP13_MgO() + scrap.getP12_CaO() + scrap.getP05_Si() * 2.139) * 10 +
                                    (limeAddition_uCons.getValue() * limeCaO.getValue() + dololimeAddition_uCons.getValue() * doloCaO.getValue()) / 100 +
                                    (limeAddition_uCons.getValue() * limeMgO.getValue() + dololimeAddition_uCons.getValue() * doloMgO.getValue()) / 100 +
                                    (limeAddition_uCons.getValue() * limeAl2O3.getValue() + dololimeAddition_uCons.getValue() * doloAl2O3.getValue()) / 100 +
                                    (limeAddition_uCons.getValue() * limeSiO2.getValue() + dololimeAddition_uCons.getValue() * doloSiO2.getValue()) / 100 +
                                    (chargeCoke_uCons.getValue() * chargeMgO.getValue() + injectionCoke_uCons.getValue() * injectionMgO.getValue()) / 100 +
                                    (chargeCoke_uCons.getValue() * chargeAl2O3.getValue() + injectionCoke_uCons.getValue() * injectionAl2O3.getValue()) / 100 +
                                    (chargeCoke_uCons.getValue() * chargeSiO2.getValue() + injectionCoke_uCons.getValue() * injectionSiO2.getValue()) / 100 +
                                    (chargeCoke_uCons.getValue() * chargeCaO.getValue() + injectionCoke_uCons.getValue() * injectionCaO.getValue()) / 100
                    ) / (1 - FeOSlag.getValue() / 100)) * percentage;
                });
                slag[0] /= 100;

                mixture.getScrapDoubleMap().forEach((scrap, percentage) -> {

                    yield[0] += (scrap.getP09_Fe() - slag[0] * FeOSlag.getValue() * 56/72/1000 + 0) * percentage; // fixme: sondaki 0 değeri excel tablosunda $D$69 ile ifade edilmiş

                });
                yield[0] /= 100;

                mixture.getScrapDoubleMap().forEach((scrap, percentage) -> {

                    dH[0] += (scrap.getP09_Fe() * 3.57 +
                            scrap.getP15_SiO2() * 4.83 +
                            scrap.getP12_CaO() * 3.75 +
                            (scrap.getP10_O() * 56/16) * 4.44 +
                            (tapTemp.getValue() - 1527) * 0.3 +
                            (scrap.getP10_O() * 56/16) * 58/1000 +
                            (scrap.getP24_meltingFactor() * percentage)) * percentage;
                });
                dH[0] = dH[0] / 100 + slag[0] * 0.46;

                dH[0] = Math.round(dH[0] * 100) / 100.0;
                slag[0] = Math.round(slag[0] * 100) / 100.0;
                yield[0] = Math.round(yield[0] * 100) / 100.0;

                Calculation calculation = new Calculation(dH[0], yield[0], slag[0]);

                runOnUiThread(Utilities::hideLoadingDialog);

                String msgTv = "Entalpi: " + calculation.getdH() + "\nYield: " + calculation.getYield() + "\nSlag: " + calculation.getSlag();
                runOnUiThread(() -> tvCalculatedEnergy.setText(msgTv));

                String msg = "Entalpi: " + calculation.getdH() + " Yield: " + calculation.getYield() + " Slag: " + calculation.getSlag();
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
            } catch (OperationFailedException e) {
                throw new RuntimeException(e);
            }


        }).start();
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
        btnCalculate = findViewById(R.id.activity_create_mixture_btn_calculate);

        recyclerView = findViewById(R.id.activity_home_page_recycler_view);
        new Thread(() -> {
            Looper.prepare();

            runOnUiThread(() -> Utilities.showLoadingDialog(CreateMixtureActivity.this, "Hurdalar yükleniyor..."));

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
            runOnUiThread(Utilities::hideLoadingDialog);
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