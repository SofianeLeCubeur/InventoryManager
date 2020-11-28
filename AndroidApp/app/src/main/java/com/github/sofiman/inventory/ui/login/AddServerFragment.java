package com.github.sofiman.inventory.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.sofiman.inventory.ui.home.HomeActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.utils.Callback;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class AddServerFragment extends Fragment {

    private final ConstraintLayout loadingLayout;
    private final boolean embed, confirmPassword;
    private final String serverIp;
    private final String serverName;
    private final @StringRes int action;
    private TextView username;
    private TextView password;

    public AddServerFragment(ConstraintLayout loadingLayout, boolean embed, String serverIp, String serverName, boolean confirmPassword, @StringRes int action) {
        this.loadingLayout = loadingLayout;
        this.embed = embed;
        this.serverIp = serverIp;
        this.serverName = serverName;
        this.confirmPassword = confirmPassword;
        this.action = action;
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

        if (embed) {
            layout.findViewById(R.id.login_more_panel).setVisibility(View.GONE);
        } else {
            layout.findViewById(R.id.login_offline).setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), HomeActivity.class);
                intent.putExtra("selectedItemId", R.id.navigation_settings);
                startActivity(intent);
                getActivity().finish();
            });
        }

        if (serverIp != null) {
            server.setText(serverIp);
            layout.findViewById(R.id.login_server_field).setVisibility(View.GONE);
        }

        EditText passwordConfirm = layout.findViewById(R.id.login_password_confirm);
        if (confirmPassword) {
            layout.findViewById(R.id.login_confirm_button).setVisibility(View.VISIBLE);
        }

        if (action != 0) {
            TextView view = layout.findViewById(R.id.login_server_action);
            view.setText(action);
        }

        Button button = layout.findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String ip = server.getText().toString().trim(), id = username.getText().toString(), secret = password.getText().toString(),
                        confirm_secret = passwordConfirm.getText().toString();
                if (ip.isEmpty()) {
                    return;
                }
                if (confirmPassword) {
                    if (!secret.equals(confirm_secret)) {
                        System.out.println("Warning: passwords does not match, skipping");
                        passwordConfirm.setText(null);
                        return;
                    }
                }
                username.setEnabled(false);
                password.setEnabled(false);
                loadingLayout.setVisibility(View.VISIBLE);

                long p = Math.round(Math.random() * 9000 + 1000);
                String name = "Inventory Server " + p;
                if(serverName != null){
                    name = serverName;
                }
                Server toConnect = new Server(name, ip + (ip.endsWith("/") ? "" : "/"));

                Callback<RequestError> callback = e -> {
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
                };

                if (confirmPassword) {
                    register(toConnect, id, secret, callback);
                } else {
                    login(toConnect, id, secret, callback);
                }
            }
        });
    }

    private void register(Server server, String id, String secret, Callback<RequestError> onFail){
        final Fetcher fetcher = Fetcher.getInstance();
        try {
            fetcher.init(getContext(), server);
            fetcher.register(id, secret, data -> {
                if (data == null) {
                    server.setAsDefaultServer(true);
                    addServer(server, id, secret);
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else if (onFail != null) {
                    onFail.run(data);
                }
            });
        } catch (Exception e){
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.login_page_no_connection, e.getMessage()), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }

    private void login(Server server, String id, String secret, Callback<RequestError> onFail) {
        final Fetcher fetcher = Fetcher.getInstance();
        try {
            fetcher.init(getContext(), server);
            fetcher.login(id, secret, data -> {
                if (data == null) {
                    addServer(server, id, secret);
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else if (onFail != null) {
                    onFail.run(data);
                }
            });
        } catch (Exception e) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.login_page_no_connection, e.getMessage()), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }

    private void addServer(Server server, String username, String password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> servers = new HashSet<>();
        if (sharedPreferences.contains("servers")) {
            servers = sharedPreferences.getStringSet("servers", new HashSet<>());
        }

        if (servers == null) {
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
        System.out.println("Saving new server: " + item.toString());

        Fetcher.getInstance().getServerList().add(new Pair<>(server, new Pair<>(username, password)));
    }
}
