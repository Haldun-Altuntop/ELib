package arc.haldun.mylibrary.main.profile;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import arc.haldun.mylibrary.R;

public class ProfileActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    ViewPager2 viewPager;
    TabLayout tabLayout;
    ViewPagerAdaptor viewPagerAdaptor;
    Toolbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init(); // Init contents

        //
        // Init actionbar
        //
        setSupportActionBar(actionbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.account));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        viewPager.setAdapter(viewPagerAdaptor);

        tabLayout.addOnTabSelectedListener(this);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    tab.select();
                }
            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    private void init() {

        viewPager = findViewById(R.id.activity_profile_viewpager);
        tabLayout = findViewById(R.id.activity_profile_tablayout);
        actionbar = findViewById(R.id.activity_profile_actionbar);
        viewPagerAdaptor = new ViewPagerAdaptor(this);
    }
}