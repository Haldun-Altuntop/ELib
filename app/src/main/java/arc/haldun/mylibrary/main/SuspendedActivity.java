package arc.haldun.mylibrary.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import arc.haldun.mylibrary.R;

public class SuspendedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspended);
    }
}