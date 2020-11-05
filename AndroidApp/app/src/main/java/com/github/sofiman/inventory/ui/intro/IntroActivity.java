package com.github.sofiman.inventory.ui.intro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.ui.login.LoginActivity;
import com.github.sofiman.inventory.ui.login.ViewPagerAdapter;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ViewPager2 pager = findViewById(R.id.intro_pager);
        TabLayout tabLayout = findViewById(R.id.intro_indicator);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);

        adapter.addFragment(new PresentationFragment());
        adapter.addFragment(new PrivacyFragment());

        pager.setAdapter(adapter);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, pager,
                (tab, position) -> tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED));
        mediator.attach();

        findViewById(R.id.intro_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableIntro();
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void disableIntro(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean("iintro", true).apply();
        System.out.println("Intro disabled");
    }

}