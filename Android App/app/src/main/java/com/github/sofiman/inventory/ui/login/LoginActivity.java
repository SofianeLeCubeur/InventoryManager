package com.github.sofiman.inventory.ui.login;


import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sofiman.inventory.HomeActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private TextView username;
    private TextView password;
    private ConstraintLayout loadingLayout;
    private List<Pair<Server, Pair<String, String>>> serverList;
    private boolean autoconnect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadSettings();

        boolean embed = getIntent().getBooleanExtra("embed", false);
        boolean autoconnect = this.autoconnect && getIntent().getBooleanExtra("autoconnect", true);

        final ConstraintLayout layout = findViewById(R.id.login_layout);
        LayoutHelper.addStatusBarOffset(this, layout);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
        params.height = params.height + LayoutHelper.getStatusBarHeight(this);
        layout.setLayoutParams(params);
        ConstraintLayout.LayoutParams llp = (ConstraintLayout.LayoutParams) findViewById(R.id.login_body).getLayoutParams();
        llp.setMargins(llp.leftMargin, llp.topMargin + LayoutHelper.getStatusBarHeight(this), llp.rightMargin, llp.bottomMargin);

        loadingLayout = findViewById(R.id.login_loading);

        ViewPager pager = findViewById(R.id.login_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if(serverList != null && serverList.size() > 0 && !embed){
            adapter.addFragment(new ServerListFragment(serverList, loadingLayout), "Login");
        }

        adapter.addFragment(new AddServerFragment(loadingLayout), "Register");

        pager.setAdapter(adapter);

        if(embed){
            ImageView back = findViewById(R.id.login_back);
            back.setVisibility(View.VISIBLE);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    supportFinishAfterTransition();
                }
            });
        }

        if (autoconnect) {
            autoConnect();
        } else if(!embed){
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
        login(first.first, first.second.first, first.second.second, new Callback<RequestError>() {
            @Override
            public void run(RequestError error) {
                connect(list, idx + 1);
            }
        });
    }
}