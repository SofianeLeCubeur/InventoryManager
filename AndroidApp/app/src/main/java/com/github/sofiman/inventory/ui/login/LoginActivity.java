package com.github.sofiman.inventory.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sofiman.inventory.ui.home.HomeActivity;
import com.github.sofiman.inventory.ui.intro.IntroActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private ConstraintLayout loadingLayout;
    private List<Pair<Server, Pair<String, String>>> serverList;
    private boolean autoconnect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadSettings();

        final Intent intent = getIntent();
        boolean embed = intent.getBooleanExtra("embed", false);
        String serverIp = intent.getStringExtra("server");
        boolean confirm = intent.getBooleanExtra("confirm_password", false);
        autoconnect = this.autoconnect && intent.getBooleanExtra("autoconnect", true);
        int action = intent.getIntExtra("action", 0);

        final ConstraintLayout layout = findViewById(R.id.login_layout);
        LayoutHelper.addStatusBarOffset(this, layout);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
        params.height = params.height + LayoutHelper.getStatusBarHeight(this);
        layout.setLayoutParams(params);
        ConstraintLayout.LayoutParams llp = (ConstraintLayout.LayoutParams) findViewById(R.id.login_body).getLayoutParams();
        llp.setMargins(llp.leftMargin, llp.topMargin + LayoutHelper.getStatusBarHeight(this), llp.rightMargin, llp.bottomMargin);

        loadingLayout = findViewById(R.id.login_loading);

        ViewPager2 pager = findViewById(R.id.login_pager);
        TabLayout tabLayout = findViewById(R.id.indicator);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);

        if (serverList != null && serverList.size() > 0 && !embed) {
            adapter.addFragment(new ServerListFragment(serverList, loadingLayout));
        }

        adapter.addFragment(new AddServerFragment(loadingLayout, embed, serverIp, confirm, action));

        pager.setAdapter(adapter);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, pager, (tab, position) -> tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED));
        mediator.attach();

        if (embed) {
            ImageView back = findViewById(R.id.login_back);
            back.setVisibility(View.VISIBLE);
            back.setOnClickListener(view -> supportFinishAfterTransition());
            tabLayout.setVisibility(View.INVISIBLE);
        }

        if (autoconnect) {
            autoConnect();
        } else if (!embed) {
            TextView info = findViewById(R.id.login_info);
            info.setText(R.string.settings_login_autoconnect_info);
        }
    }

    private void login(Server server, String id, String secret, Callback<RequestError> onFail) {
        final Fetcher fetcher = Fetcher.getInstance();
        fetcher.init(server);
        fetcher.login(id, secret, new Callback<RequestError>() {
            @Override
            public void run(RequestError data) {
                if (data == null) {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else if (onFail != null) {
                    onFail.run(data);
                }
            }
        });
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("Showing intro: " + !sharedPreferences.contains("iintro"));
        if (!sharedPreferences.contains("iintro") || !sharedPreferences.getBoolean("iintro", false)) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
            return;
        }
        if (sharedPreferences.contains("servers")) {
            serverList = new ArrayList<>();
            Set<String> servers = sharedPreferences.getStringSet("servers", new HashSet<>());

            JsonObject object;
            String name, url, id, secret;
            boolean defaultServer;
            for (String json : servers) {
                System.out.println(json);
                try {
                    object = JsonParser.parseString(json).getAsJsonObject();
                    name = object.get("name").getAsString();
                    url = object.get("url").getAsString();
                    id = object.get("uid").getAsString();
                    secret = object.get("ust").getAsString();
                    defaultServer = object.get("default").getAsBoolean();

                    serverList.add(new Pair<>(new Server(name, url).setAsDefaultServer(defaultServer), new Pair<>(id, secret)));
                } catch (Exception e) {
                    System.err.println("Could not parse preference server json: " + json);
                    e.printStackTrace();
                }
            }

            Collections.sort(serverList, (t1, t2) -> Boolean.compare(t2.first.isDefaultServer(), t1.first.isDefaultServer()));
            Fetcher.getInstance().setServerList(serverList);
        }
        this.autoconnect = sharedPreferences.getBoolean("autoconnect", true);
    }

    private void autoConnect() {
        loadingLayout.setVisibility(View.VISIBLE);
        if (serverList != null && serverList.size() > 0) {
            connect(serverList, 0);
            return;
        }
        System.out.println("Auto Connect failed, waiting for user connection");
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    private void connect(List<Pair<Server, Pair<String, String>>> list, int idx) {
        if (idx >= list.size()) {
            loadingLayout.setVisibility(View.INVISIBLE);
            return;
        }
        Pair<Server, Pair<String, String>> first = list.get(idx);
        try {
            login(first.first, first.second.first, first.second.second, error -> connect(list, idx + 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}