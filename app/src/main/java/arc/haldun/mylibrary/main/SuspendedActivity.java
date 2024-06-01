package arc.haldun.mylibrary.main;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import arc.haldun.mylibrary.R;

public class SuspendedActivity extends AppCompatActivity {

    private TextView tv_label;

    Runnable actionTextSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspended);
        init();

        actionTextSession = () -> {

            String msg = tv_label.getText().toString();
            int length = msg.length();
            tv_label.setText("");

            for (int i = 0; i < length; i++) {
                char c = msg.charAt(i);

                setText(tv_label.getText().toString() + c);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        };

        new Thread(actionTextSession).start();
    }

    private void setText(String str) {

        Handler handler = new Handler(getMainLooper());
        handler.post(() -> {
            tv_label.setText(str);
            synchronized (handler) {
                handler.notify();
            }
        });
        try {
            synchronized (handler) {
                handler.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void init() {
        tv_label = findViewById(R.id.activity_suspended_tv_label);
    }
}