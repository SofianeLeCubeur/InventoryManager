package com.github.sofiman.inventory.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.impl.HistoryDataModel;
import com.github.sofiman.inventory.ui.dialogs.DoubleEditDialog;
import com.github.sofiman.inventory.ui.login.LoginActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.model.ServerListAdapter;
import com.github.sofiman.inventory.utils.Animations;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SettingsFragment extends Fragment {

    private ServerListAdapter serverListAdapter;
    private Animations.Debouncer debouncer = new Animations.Debouncer();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        ConstraintLayout layout = root.findViewById(R.id.settings_header);
        LayoutHelper.addStatusBarOffset(getContext(), layout);

        RecyclerView recyclerView = root.findViewById(R.id.settings_server_list);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        serverListAdapter = new ServerListAdapter(getContext(), Fetcher.getInstance().getServerList());
        recyclerView.setAdapter(serverListAdapter);

        serverListAdapter.setListeners(new Callback<Pair<Server, Pair<String, String>>>() {
            @Override
            public void run(Pair<Server, Pair<String, String>> data) {
                edit(data);
            }
        }, new Callback<Pair<Server, Pair<String, String>>>() {
            @Override
            public void run(Pair<Server, Pair<String, String>> data) {
                deleteServer(data);
            }
        }, new Callback<Pair<Server, Pair<String, String>>>() {
            @Override
            public void run(Pair<Server, Pair<String, String>> data) {
                debouncer.debounce("save_servers", new Runnable() {
                    @Override
                    public void run() {
                        saveServers();
                    }
                }, 1, TimeUnit.SECONDS);
            }
        });

        ImageView button = root.findViewById(R.id.settings_register);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

        SwitchMaterial autoConnectSwitch = root.findViewById(R.id.settings_login_autoconnect);
        autoConnectSwitch.setChecked(isAutoconnectEnabled());
        autoConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                debouncer.debounce("auto_connect", new Runnable() {
                    @Override
                    public void run() {
                        setAutoconnect(b);
                    }
                }, 750, TimeUnit.MILLISECONDS);
            }
        });

        SwitchMaterial registerUnknownCodesSwitch = root.findViewById(R.id.settings_scan_register_unknown);
        registerUnknownCodesSwitch.setChecked(canRegisterUnknownCodes());
        registerUnknownCodesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                debouncer.debounce("register_unknown_codes", new Runnable() {
                    @Override
                    public void run() {
                        setRegisterUnknownCodes(b);
                    }
                }, 750, TimeUnit.MILLISECONDS);
            }
        });

        TextView clearScanHistory = root.findViewById(R.id.settings_clear_scan_history);
        clearScanHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearScanLog();
            }
        });

        ConstraintLayout trackerDetails = root.findViewById(R.id.settings_scan_as_tracker_details);
        TextView trackerName = root.findViewById(R.id.settings_scan_as_tracker_name);
        ImageView trackerEdit = root.findViewById(R.id.settings_scan_as_tracker_edit);

        SwitchMaterial useDeviceAsTracker = root.findViewById(R.id.settings_scan_as_tracker);
        useDeviceAsTracker.setChecked(isScanAsTrackerEnabled());
        useDeviceAsTracker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Animations.expand(trackerDetails);
                    trackerEdit.setEnabled(true);
                    debouncer.debounce("toggle_device_as_tracker", new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("scanning_as_tracker", true);
                            editor.apply();
                        }
                    }, 750, TimeUnit.MILLISECONDS);
                } else {
                    Animations.collapse(trackerDetails);
                    trackerEdit.setEnabled(false);
                    debouncer.debounce("toggle_device_as_tracker", new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("scanning_as_tracker", false);
                            editor.apply();
                        }
                    }, 750, TimeUnit.MILLISECONDS);
                }
            }
        });
        trackerEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String name = sharedPreferences.getString("scan_tracker_name", HistoryDataModel.getDeviceName());
                String location = sharedPreferences.getString("scan_tracker_location", "");
                editTracker(name, location, new Callback<String>() {
                    @Override
                    public void run(String name) {
                        trackerName.setText(name);
                    }
                });
            }
        });
        if (useDeviceAsTracker.isChecked()) {
            trackerDetails.setVisibility(View.VISIBLE);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            trackerName.setText(sharedPreferences.getString("scan_tracker_name", HistoryDataModel.getDeviceName()));
        }

        return root;
    }

    @Override
    public void onDetach() {
        saveServers();
        View root = getView();
        if (root != null) {
            SwitchMaterial autoConnectSwitch = root.findViewById(R.id.settings_login_autoconnect);
            setAutoconnect(autoConnectSwitch.isChecked());
            SwitchMaterial registerUnknownCodesSwitch = root.findViewById(R.id.settings_scan_register_unknown);
            setRegisterUnknownCodes(registerUnknownCodesSwitch.isChecked());
        }
        super.onDetach();
    }

    private void edit(Pair<Server, Pair<String, String>> store) {
        final Server server = store.first;

        DoubleEditDialog dialog = new DoubleEditDialog(getContext(), getLayoutInflater(),
                new DataField(0, server.getName(), getString(R.string.dialog_edit_server_name)),
                new DataField(0, server.getEndpoint(), getString(R.string.dialog_edit_server_address)));

        AlertDialog handle = dialog.getDialog();

        dialog.setButtons(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String serverName = dialog.getFirstField().getText().toString().trim();
                        if (!serverName.isEmpty()) {
                            server.setName(serverName);
                            serverListAdapter.notifyItemChanged(serverListAdapter.getServers().indexOf(store));
                            saveServers();
                            dialogInterface.dismiss();
                        }
                    }
                },
                getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE);

        handle.setTitle(getString(R.string.dialog_edit_server, server.getName()));
        handle.show();
    }

    private void editTracker(String deviceName, String location, Callback<String> edited) {
        DoubleEditDialog dialog = new DoubleEditDialog(getContext(), getLayoutInflater(),
                new DataField(0, deviceName, getString(R.string.dialog_edit_tracker_name)),
                new DataField(0, location, getString(R.string.dialog_edit_tracker_location)));

        AlertDialog handle = dialog.getDialog();
        handle.setTitle(getString(R.string.dialog_edit_tracker));

        dialog.setButtons(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String name = dialog.getFirstField().getText().toString();
                final String location = dialog.getSecondField().getText().toString();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("scanning_as_tracker", true);
                editor.putString("scan_tracker_name", name);
                editor.putString("scan_tracker_location", location);
                editor.apply();
                edited.run(name);
            }
        }, getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE);

        handle.show();
    }

    private void deleteServer(Pair<Server, Pair<String, String>> store) {
        final Server server = store.first;
        showConfirmDialog(getString(R.string.dialog_confirm, getString(R.string.dialog_confirm_delete)),
                getString(R.string.dialog_delete_confirmation, server.getName()), getString(R.string.dialog_delete), new Runnable() {
            @Override
            public void run() {
                serverListAdapter.notifyItemRemoved(serverListAdapter.getServers().indexOf(store));
                serverListAdapter.getServers().remove(store);
                saveServers();
                if (serverListAdapter.getServers().size() <= 0) {
                    openLoginActivity();
                }
            }
        });
    }

    private void clearScanLog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int count = sharedPreferences.getStringSet("scan_log", new HashSet<>()).size();
        showConfirmDialog(getString(R.string.dialog_confirm, getString(R.string.dialog_confirm_clear)),
                getString(R.string.dialog_clear_scan, count), getString(R.string.dialog_clear), new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("scan_log");
                editor.apply();
                Toast.makeText(getContext(), R.string.settings_scan_history_cleared, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmDialog(String title, String message, String positiveButton, Runnable deleteCallback) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(title).setMessage(message)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteCallback.run();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.colorAccent));
            }
        });

        alertDialog.show();
    }

    private void openLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.putExtra("autoconnect", false);
        intent.putExtra("embed", true);
        startActivity(intent);
    }

    private void saveServers() {
        System.out.println("Saving servers now");
        List<Pair<Server, Pair<String, String>>> serverList = serverListAdapter.getServers();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> servers = new HashSet<>();

        JsonObject item;
        for (Pair<Server, Pair<String, String>> entry : serverList) {
            Server server = entry.first;
            item = new JsonObject();
            item.addProperty("name", server.getName());
            item.addProperty("url", server.getEndpoint());
            item.addProperty("uid", entry.second.first);
            item.addProperty("ust", entry.second.second);
            item.addProperty("default", server.isDefaultServer());
            servers.add(item.toString());
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("servers", servers);
        editor.apply();
        Fetcher.getInstance().setServerList(serverList);
    }

    private void setAutoconnect(boolean autoconnect) {
        System.out.println("Saving autoconnect state now");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("autoconnect", autoconnect);
        editor.apply();
    }

    private boolean isAutoconnectEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean("autoconnect", true);
    }

    private boolean isScanAsTrackerEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean("scanning_as_tracker", true);
    }

    private void setRegisterUnknownCodes(boolean registerUnknownCodes) {
        System.out.println("Saving registerUnknownCodes state now");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("register_unknown_codes", registerUnknownCodes);
        editor.apply();
    }

    private boolean canRegisterUnknownCodes() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean("register_unknown_codes", false);
    }

    /**public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, AndroidLocalize.class);
        finish();
        startActivity(refresh);
    }*/
}