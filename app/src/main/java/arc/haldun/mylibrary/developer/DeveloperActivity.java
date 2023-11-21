package arc.haldun.mylibrary.developer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Tools;

public class DeveloperActivity extends AppCompatActivity {

    View.OnClickListener onClickListener;

    Button btn_externalJARFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        init();

        btn_externalJARFile.setOnClickListener(onClickListener);
    }

    private void init() {

        //
        // Views
        //

        btn_externalJARFile = findViewById(R.id.activity_developer_btn_external_jar_file);

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
        };
    }

    public void makeDialog(Context context, CharSequence tittle, CharSequence message,
                                  CharSequence buttonText,
                                  DialogInterface.OnClickListener onClickListener) {


    }
}