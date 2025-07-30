package arc.haldun.hurda.mobile.parameters;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import arc.haldun.hurda.api.ScrapBridge;
import arc.haldun.hurda.database.OperationFailedException;
import arc.haldun.hurda.database.objects.GeneralParameter;
import arc.haldun.hurda.mobile.LoginActivity;
import arc.haldun.hurda.mobile.R;
import arc.haldun.hurda.mobile.Utilities;

public class SetParametersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GeneralParameterAdapter parameterAdapter;
    private FloatingActionButton fab;
    private Toolbar actionBar;
    private AlertDialog addParameterDialog, updateParameterDialog;
    private ProgressBar progressBar;

    private View addParameterDialogPane;
    private EditText etParameterName_add, etParameterValue_add;

    private View updateParameterPane;
    private EditText etParameterName_update, etParameterValue_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_patameters);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initActionbar();
        initRecyclerView();

        fab.setOnClickListener(this::addParameterClick);
    }

    private void initRecyclerView() {

        new Thread(() -> {
            try {
                GeneralParameter[] parameters = ScrapBridge.getAllGeneralParameters();

                runOnUiThread(() -> {

                    parameterAdapter = new GeneralParameterAdapter(parameters);
                    parameterAdapter.setOnItemClicked(this::onItemClicked);
                    parameterAdapter.setOnItemLongClicked(this::onItemLongClicked);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

                    FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getApplicationContext());
                    flexboxLayoutManager.setFlexDirection(FlexDirection.ROW); // Yatayda diz
                    flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);          // Taşarsa alt satıra geç
                    flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START); // Sola hizalı


                    recyclerView.setLayoutManager(flexboxLayoutManager);
                    recyclerView.setAdapter(parameterAdapter);

                    progressBar.setVisibility(View.GONE);
                });


            } catch (OperationFailedException e) {
                runOnUiThread(() -> Utilities.unauthorizedUserDetected(this));
            }
        }).start();
    }

    private void onItemClicked(GeneralParameter parameter) {

        etParameterName_update.setText(parameter.getName());
        etParameterValue_update.setText(String.valueOf(parameter.getValue()));

        updateParameterDialog.show();
    }

    private void onItemLongClicked(GeneralParameter parameter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(parameter.getName())
                .setMessage(getString(R.string.parameter_will_deleted))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    Utilities.showLoadingDialog(this, getString(R.string.parameter_is_deleting));
                    deleteParameter(parameter);
                })
                .setNegativeButton(getString(R.string.no), null);

        builder.create().show();
    }

    private void addParameterClick(View v) {

        addParameterDialog.show();
    }

    private void addParameter() {

        GeneralParameter parameter = new GeneralParameter(
                etParameterName_add.getText().toString(),
                Double.parseDouble(etParameterValue_add.getText().toString())
        );

        new Thread(() -> {

            try {
                ScrapBridge.addGeneralParameter(parameter);

                runOnUiThread(() -> {
                    parameterAdapter.addParameter(parameter);
                    Utilities.hideLoadingDialog();
                    addParameterDialog.dismiss();
                });

            } catch (OperationFailedException e) {
                if (Objects.requireNonNull(e.getMessage()).contains("Yetkisiz kullanıcı")) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), R.string.unauthorized_user_message, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    });
                } else throw new RuntimeException(e);
            }

        }).start();
    }

    private void updateParameter() {

        GeneralParameter parameter = new GeneralParameter(
                etParameterName_update.getText().toString(),
                Double.parseDouble(etParameterValue_update.getText().toString())
        );

        new Thread(() -> {

            try {
                ScrapBridge.updateGeneralParameter(parameter);
                runOnUiThread(() -> {

                    Utilities.hideLoadingDialog();

                    parameterAdapter.updateParameter(parameter);
                });

            } catch (OperationFailedException e) {
                if (Objects.requireNonNull(e.getMessage()).contains("Yetkisiz kullanıcı")) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), R.string.unauthorized_user_message, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    });
                } else throw new RuntimeException(e);
            }

        }).start();
    }

    private void deleteParameter(GeneralParameter parameter) {

        new Thread(() -> {

            try {
                ScrapBridge.deleteGeneralParameter(parameter.getName());
                runOnUiThread(() -> {

                    Utilities.hideLoadingDialog();

                    parameterAdapter.removeParameter(parameter);
                });

            } catch (OperationFailedException e) {
                if (Objects.requireNonNull(e.getMessage()).contains("Yetkisiz kullanıcı")) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), R.string.unauthorized_user_message, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    });
                } else throw new RuntimeException(e);
            }

        }).start();
    }

    private void initActionbar() {

        setSupportActionBar(actionBar);

        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(getString(R.string.app_name));
            supportActionbar.setDisplayShowTitleEnabled(true);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.activity_set_parameters_recycler_view);
        fab = findViewById(R.id.activity_set_parameters_fab);
        actionBar = findViewById(R.id.activity_set_parameters_action_bar);
        progressBar = findViewById(R.id.activity_set_parameters_progress_bar);

        addParameterDialogPane = LayoutInflater.from(this).inflate(R.layout.dialog_add_parameter, null); // fixme: find warning cause
        etParameterName_add = addParameterDialogPane.findViewById(R.id.dialog_add_parameter_et_parameter_name);
        etParameterValue_add = addParameterDialogPane.findViewById(R.id.dialog_add_parameter_et_parameter_value);

        updateParameterPane = LayoutInflater.from(this).inflate(R.layout.dialog_add_parameter, null);// fixme: find warning cause
        etParameterName_update = updateParameterPane.findViewById(R.id.dialog_add_parameter_et_parameter_name);
        etParameterValue_update = updateParameterPane.findViewById(R.id.dialog_add_parameter_et_parameter_value);
        etParameterName_update.setEnabled(false);

        initDialog();
    }

    private void initDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle(getString(R.string.add_new_parameter))
                .setView(addParameterDialogPane)
                .setPositiveButton(getString(R.string.add), (dialog, which) -> {
                    Utilities.showLoadingDialog(this, getString(R.string.parameter_is_adding));
                    addParameter();
                })
                .setNegativeButton(getString(R.string.cancel), null);

        addParameterDialog = builder1.create();

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle(getString(R.string.update_parameter))
                .setView(updateParameterPane)
                .setPositiveButton(getString(R.string.update_parameter), (dialog, which) -> {
                    Utilities.showLoadingDialog(this, getString(R.string.parameter_is_updating));
                    updateParameter();
                })
                .setNegativeButton(getString(R.string.cancel), null);

        updateParameterDialog = builder2.create();
    }
}