package com.github.sofiman.inventory;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
        LayoutHelper.addStatusBarOffset(this, pager);

       // FrameLayout layout = findViewById(R.id.intro_display_header);
       // layout.getLayoutParams().height += LayoutHelper.getStatusBarHeight(this);

        adapter.addFragment(new Display(R.string.intro_presentation, R.drawable.mc_ship_dock_2x_peter_michael_perceval_iii, R.string.intro_presentation_desc));
        adapter.addFragment(new Display(R.string.intro_presentation, R.drawable.mc_ship_dock_2x_peter_michael_perceval_iii, R.string.intro_presentation_desc));
        adapter.addFragment(new Display(R.string.intro_presentation, R.drawable.mc_ship_dock_2x_peter_michael_perceval_iii, R.string.intro_presentation_desc));
        adapter.addFragment(new Display(R.string.intro_presentation, R.drawable.mc_ship_dock_2x_peter_michael_perceval_iii, R.string.intro_presentation_desc));

        pager.setAdapter(adapter);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
            }
        });
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

    public static class Display extends Fragment {

        private @StringRes int title;
        private @DrawableRes int illustration;
        private @StringRes int description;

        public Display(@StringRes int title, @DrawableRes int illustration, @StringRes  int description) {
            this.title = title;
            this.illustration = illustration;
            this.description = description;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_intro_display, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            ImageView illustrationView = view.findViewById(R.id.intro_display_illustration);
            illustrationView.setImageResource(illustration);
            TextView titleView = view.findViewById(R.id.intro_display_title);
            titleView.setText(title);
            TextView descView = view.findViewById(R.id.intro_display_desc);
            descView.setText(description);
        }
    }
}