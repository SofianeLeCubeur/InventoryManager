package com.github.sofiman.inventory.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.sofiman.inventory.HomeActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.StringUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class AddServerFragment extends Fragment {

    private TextView username;
    private TextView password;
    private ConstraintLayout loadingLayout;

    public AddServerFragment(ConstraintLayout loadingLayout){
        this.loadingLayout = loadingLayout;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_server_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View layout, @Nullable Bundle savedInstanceState) {
        final TextView server = layout.findViewById(R.id.login_server_ip);
        username = layout.findViewById(R.id.login_username);
        password = layout.findViewById(R.id.login_password);

        Button button = layout.findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String ip = server.getText().toString().trim(), id = username.getText().toString(), secret = password.getText().toString();
                if (ip.isEmpty()) {
                    return;
                }
                username.setEnabled(false);
                password.setEnabled(false);
                loadingLayout.setVisibility(View.VISIBLE);

                long p = Math.round(Math.random() * 1000);
                Server toConnect = new Server("Inventory Server " + p, ip);

                login(toConnect, id, secret, new Callback<RequestError>() {
                    @Override
                    public void run(RequestError e) {
                        System.err.println("Could not connect to server " + ip + ":" + e);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLayout.setVisibility(View.GONE);
                                username.setEnabled(true);
                                password.setEnabled(true);
                                password.setText("");
                                Toast.makeText(getContext(), getString(R.string.login_page_no_connection, e.toString()), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void login(Server server,  String id, String secret, Callback<RequestError> onFail) {
        final Fetcher fetcher = Fetcher.getInstance();
        try {
            fetcher.init(server);
            fetcher.login(id, secret, new Callback<RequestError>() {
                @Override
                public void run(RequestError data) {
                    if (data == null) {
                        addServer(server, id, secret);
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (onFail != null) {
                        onFail.run(data);
                    }
                }
            });
        } catch (Exception e){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getString(R.string.login_page_no_connection, e.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        }
    }

    private void addServer(Server server, String username, String password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> servers = new HashSet<>();
        if (sharedPreferences.contains("servers")) {
            servers = sharedPreferences.getStringSet("servers", new HashSet<>());
        }

        if(servers == null){
            servers = new HashSet<>();
        }

        JsonObject item = new JsonObject();
        item.addProperty("name", server.getName());
        item.addProperty("url", server.getEndpoint());
        item.addProperty("uid", username);
        item.addProperty("ust", password);
        item.addProperty("default", servers.size() == 0);

        servers.add(item.toString());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("servers", servers);
        editor.apply();

        Fetcher.getInstance().getServerList().add(new Pair<>(server, new Pair<>(username, password)));
    }
}
