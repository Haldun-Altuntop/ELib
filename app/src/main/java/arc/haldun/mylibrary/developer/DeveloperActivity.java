package arc.haldun.mylibrary.developer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import arc.haldun.mylibrary.R;

public class DeveloperActivity extends AppCompatActivity {

    View.OnClickListener onClickListener;

    Button btn_externalJARFile, btn_throwException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        init();

        btn_externalJARFile.setOnClickListener(onClickListener);
        btn_throwException.setOnClickListener(onClickListener);
    }

    private void init() {

        //
        // Views
        //

        btn_externalJARFile = findViewById(R.id.activity_developer_btn_external_jar_file);
        btn_throwException = findViewById(R.id.activity_developer_btn_throw_exception);

        //
        // Listeners
        //

        onClickListener = view -> {

            if (view == btn_externalJARFile) {

                String tittle = "External JAR File";
                String message = "Sunucularımızda yüklün olan bir java kodunu çalıştırmak üzeresiniz.";
                String buttonText = "Devam Et";

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(tittle)
                        .setMessage(message)
                        .setPositiveButton(buttonText, (dialogInterface, i) -> {

                            ExternalJARInvoker externalJARInvoker = new ExternalJARInvoker(getApplicationContext());

                            externalJARInvoker.addParameter(getApplicationContext());
                            externalJARInvoker.addParameter(findViewById(R.id.activity_developer_root_layout));
                            externalJARInvoker.addParameter(this);

                            externalJARInvoker.start();

                        });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }

            if (view == btn_throwException) {

                throw new RuntimeException("This exception thrown via developer activity.");

            }
        };
    }
}