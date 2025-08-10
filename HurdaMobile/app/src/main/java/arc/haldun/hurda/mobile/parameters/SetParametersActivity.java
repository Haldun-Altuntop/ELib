package arc.haldun.hurda.mobile.parameters;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import arc.haldun.hurda.api.ScrapBridge;
import arc.haldun.hurda.database.OperationFailedException;
import arc.haldun.hurda.database.objects.GeneralParameter;
import arc.haldun.hurda.mobile.LoginActivity;
import arc.haldun.hurda.mobile.R;
import arc.haldun.hurda.mobile.Utilities;

public class SetParametersActivity extends AppCompatActivity {

    private RecyclerView rvGeneralParameters, rvTimeBalance, rvFluxesAndOtherAdditions, rvMeltInTraceElements;
    private GeneralParameterAdapter parameterAdapter, timeBalanceAdapter, fluxesAndOtherAdditionsAdapter, meltInTraceElementsAdapter;

    private FloatingActionButton fab;
    private Toolbar actionBar;
    private AlertDialog addParameterDialog, updateParameterDialog;
    private ProgressBar progressBar;

    private View addParameterDialogPane;
    private EditText etParameterName_add, etParameterValue_add;
    private Spinner spinnerCategory;

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

                ArrayList<GeneralParameter> generalParameters = new ArrayList<>();
                ArrayList<GeneralParameter> timeBalance = new ArrayList<>();
                ArrayList<GeneralParameter> fluxesAndOtherAdditions = new ArrayList<>();
                ArrayList<GeneralParameter> meltInTraceElements = new ArrayList<>();

                for (GeneralParameter parameter : parameters) {

                    switch (parameter.getCategory()) {
                        case GENERAL:
                            generalParameters.add(parameter);
                            break;
                        case TIME_BALANCE:
                            timeBalance.add(parameter);
                            break;
                        case FLUXES_AND_OTHER_ADDITIONS:
                            fluxesAndOtherAdditions.add(parameter);
                            break;
                        case MELT_IN_TRACE_ELEMENTS:
                            meltInTraceElements.add(parameter);
                            break;
                    }
                }

                runOnUiThread(() -> {

                    // Adapters
                    parameterAdapter = new GeneralParameterAdapter(generalParameters);
                    parameterAdapter.setOnItemClicked(this::onItemClicked);
                    parameterAdapter.setOnItemLongClicked(this::onItemLongClicked);

                    timeBalanceAdapter = new GeneralParameterAdapter(timeBalance);
                    timeBalanceAdapter.setOnItemClicked(this::onItemClicked);
                    timeBalanceAdapter.setOnItemLongClicked(this::onItemLongClicked);

                    fluxesAndOtherAdditionsAdapter = new GeneralParameterAdapter(fluxesAndOtherAdditions);
                    fluxesAndOtherAdditionsAdapter.setOnItemClicked(this::onItemClicked);
                    fluxesAndOtherAdditionsAdapter.setOnItemLongClicked(this::onItemLongClicked);

                    meltInTraceElementsAdapter = new GeneralParameterAdapter(meltInTraceElements);
                    meltInTraceElementsAdapter.setOnItemClicked(this::onItemClicked);
                    meltInTraceElementsAdapter.setOnItemLongClicked(this::onItemLongClicked);

                    // Layout Managers
                    GridLayoutManager lmGeneralParameters = new GridLayoutManager(getApplicationContext(), 2) {
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }
                    };
                    GridLayoutManager lmTimeBalance = new GridLayoutManager(getApplicationContext(), 2) {
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }
                    };
                    GridLayoutManager lmMeltInTraceElements = new GridLayoutManager(getApplicationContext(), 2) {
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }
                    };
                    GridLayoutManager lmFluxesAdOtherAdditions = new GridLayoutManager(getApplicationContext(), 2) {
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }
                    };

                    // RecyclerViews
                    rvGeneralParameters.setLayoutManager(lmGeneralParameters);
                    rvGeneralParameters.setAdapter(parameterAdapter);

                    rvTimeBalance.setLayoutManager(lmTimeBalance);
                    rvTimeBalance.setAdapter(timeBalanceAdapter);

                    rvFluxesAndOtherAdditions.setLayoutManager(lmMeltInTraceElements);
                    rvFluxesAndOtherAdditions.setAdapter(fluxesAndOtherAdditionsAdapter);

                    rvMeltInTraceElements.setLayoutManager(lmFluxesAdOtherAdditions);
                    rvMeltInTraceElements.setAdapter(meltInTraceElementsAdapter);

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
                0,
                etParameterName_add.getText().toString(),
                Double.parseDouble(etParameterValue_add.getText().toString()),
                getSpinnerSelectedCategory()
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
                0,
                etParameterName_update.getText().toString(),
                Double.parseDouble(etParameterValue_update.getText().toString()),
                GeneralParameter.Category.GENERAL //TODO: Şimdilik varsayılan değer
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
        rvGeneralParameters = findViewById(R.id.activity_set_parameters_recyclerview_general_parameters);
        rvTimeBalance = findViewById(R.id.activity_set_parameters_recyclerview_time_balance);
        rvFluxesAndOtherAdditions = findViewById(R.id.activity_set_parameters_recyclerview_fluxes_and_other_additions);
        rvMeltInTraceElements = findViewById(R.id.activity_set_parameters_recyclerview_melt_int_trace_elements);

        fab = findViewById(R.id.activity_set_parameters_fab);
        actionBar = findViewById(R.id.activity_set_parameters_action_bar);
        progressBar = findViewById(R.id.activity_set_parameters_progress_bar);

        addParameterDialogPane = LayoutInflater.from(this).inflate(R.layout.dialog_add_parameter, null); // fixme: find warning cause
        etParameterName_add = addParameterDialogPane.findViewById(R.id.dialog_add_parameter_et_parameter_name);
        etParameterValue_add = addParameterDialogPane.findViewById(R.id.dialog_add_parameter_et_parameter_value);
        initSpinner();

        updateParameterPane = LayoutInflater.from(this).inflate(R.layout.dialog_add_parameter, null);// fixme: find warning cause
        etParameterName_update = updateParameterPane.findViewById(R.id.dialog_add_parameter_et_parameter_name);
        etParameterValue_update = updateParameterPane.findViewById(R.id.dialog_add_parameter_et_parameter_value);
        etParameterName_update.setEnabled(false);

        initDialog();
    }

    private void initSpinner() {
        spinnerCategory = addParameterDialogPane.findViewById(R.id.dialog_add_parameter_spinner_category);
        String[] categories = new String[] {
                GeneralParameter.Category.GENERAL.name(),
                GeneralParameter.Category.TIME_BALANCE.name(),
                GeneralParameter.Category.FLUXES_AND_OTHER_ADDITIONS.name(),
                GeneralParameter.Category.MELT_IN_TRACE_ELEMENTS.name()
        };
        SpinnerAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerCategory.setAdapter(adapter);
    }

    private GeneralParameter.Category getSpinnerSelectedCategory() {

        int position = spinnerCategory.getSelectedItemPosition();

        switch (position) {

            case 0:
                return GeneralParameter.Category.GENERAL;

            case 1:
                return GeneralParameter.Category.TIME_BALANCE;

            case 2:
                return GeneralParameter.Category.FLUXES_AND_OTHER_ADDITIONS;

            case 3:
                return GeneralParameter.Category.MELT_IN_TRACE_ELEMENTS;

            default:
                throw new RuntimeException("Hatalı kategori seçimi");
        }
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