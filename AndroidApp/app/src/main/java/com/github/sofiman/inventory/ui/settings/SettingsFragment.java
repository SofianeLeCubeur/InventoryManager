package com.github.sofiman.inventory.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
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
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.impl.HistoryDataModel;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.ui.dialogs.ConfirmDialog;
import com.github.sofiman.inventory.ui.dialogs.DoubleEditDialog;
import com.github.sofiman.inventory.ui.login.LoginActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.model.ServerListAdapter;
import com.github.sofiman.inventory.utils.Animations;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsFragment extends Fragment {

    private ServerListAdapter serverListAdapter;
    private Animations.Debouncer debouncer = new Animations.Debouncer();
    private SharedPreferences sharedPreferences;
    private boolean invalidateIntro;
    private boolean autoconnect;
    private boolean registerUnknownCodes;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = root.findViewById(R.id.settings_header);
        LayoutHelper.addStatusBarOffset(getContext(), layout);

        RecyclerView recyclerView = root.findViewById(R.id.settings_server_list);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        serverListAdapter = new ServerListAdapter(getContext(), Fetcher.getInstance().getServerList());
        recyclerView.setAdapter(serverListAdapter);

        serverListAdapter.setListeners(this::edit, this::deleteServer, data -> debouncer.debounce("save_servers", () -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            saveServers(editor);
            editor.commit();
        }, 1, TimeUnit.SECONDS));

        ImageView button = root.findViewById(R.id.settings_register);
        button.setOnClickListener(view -> openLoginActivity());

        SwitchMaterial autoConnectSwitch = root.findViewById(R.id.settings_login_autoconnect);
        autoconnect = isAutoconnectEnabled();
        autoConnectSwitch.setChecked(autoconnect);
        autoConnectSwitch.setOnCheckedChangeListener((compoundButton, b) -> debouncer.debounce("auto_connect", () -> {
            autoconnect = b;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            setAutoconnect(editor, b);
            editor.commit();
        }, 750, TimeUnit.MILLISECONDS));

        SwitchMaterial registerUnknownCodesSwitch = root.findViewById(R.id.settings_scan_register_unknown);
        registerUnknownCodes = canRegisterUnknownCodes();
        registerUnknownCodesSwitch.setChecked(registerUnknownCodes);
        registerUnknownCodesSwitch.setOnCheckedChangeListener((compoundButton, b) -> debouncer.debounce("register_unknown_codes", () -> {
            registerUnknownCodes = b;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            setRegisterUnknownCodes(editor, b);
            editor.commit();
        }, 750, TimeUnit.MILLISECONDS));

        ConstraintLayout clearScanHistory = root.findViewById(R.id.settings_clear_scan_history);
        clearScanHistory.setOnClickListener(view -> clearScanLog());

        ConstraintLayout trackerDetails = root.findViewById(R.id.settings_scan_as_tracker_details);
        TextView trackerName = root.findViewById(R.id.settings_scan_as_tracker_name);
        ImageView trackerEdit = root.findViewById(R.id.settings_scan_as_tracker_edit);

        SwitchMaterial useDeviceAsTracker = root.findViewById(R.id.settings_scan_as_tracker);
        useDeviceAsTracker.setChecked(isScanAsTrackerEnabled());
        useDeviceAsTracker.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (b) {
                Animations.expand(trackerDetails);
                trackerEdit.setEnabled(true);
                debouncer.debounce("toggle_device_as_tracker", () -> setScanningAsTracker(editor, true), 750, TimeUnit.MILLISECONDS);
            } else {
                Animations.collapse(trackerDetails);
                trackerEdit.setEnabled(false);
                debouncer.debounce("toggle_device_as_tracker", () -> setScanningAsTracker(editor, false), 750, TimeUnit.MILLISECONDS);
            }
        });
        trackerEdit.setOnClickListener(view -> {
            String name = sharedPreferences.getString("scan_tracker_name", HistoryDataModel.getDeviceName());
            String location = sharedPreferences.getString("scan_tracker_location", "");
            editTracker(name, location, trackerName::setText);
        });
        if (useDeviceAsTracker.isChecked()) {
            trackerDetails.setVisibility(View.VISIBLE);
            trackerName.setText(sharedPreferences.getString("scan_tracker_name", HistoryDataModel.getDeviceName()));
        }

        root.findViewById(R.id.settings_change_lang).setOnClickListener(v -> openSwitchLocale());

        root.findViewById(R.id.settings_intro).setOnClickListener(v -> {
            invalidateIntro = true;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            invalidateIntro(editor);
            editor.commit();
            Toast.makeText(getContext(), R.string.settings_intro_info, Toast.LENGTH_SHORT).show();
        });
        root.findViewById(R.id.settings_acknowledgements).setOnClickListener(v -> startActivity(new Intent(getContext(), Acknowledgements.class)));
    }

    @Override
    public void onDetach() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        saveServers(editor);
        setAutoconnect(editor, autoconnect);
        setRegisterUnknownCodes(editor, registerUnknownCodes);
        if (invalidateIntro) {
            invalidateIntro(editor);
        }
        editor.apply();
        super.onDetach();
    }

    private void edit(Pair<Server, Pair<String, String>> store) {
        final Server server = store.first;

        DoubleEditDialog dialog = new DoubleEditDialog(getContext(), getLayoutInflater(),
                new DataField(0, server.getName(), getString(R.string.dialog_edit_server_name)),
                new DataField(0, server.getEndpoint(), getString(R.string.dialog_edit_server_address)));

        AlertDialog handle = dialog.getDialog();

        dialog.setButtons(getString(R.string.dialog_save), (dialogInterface, i) -> {
            final String serverName = dialog.getFirstField().getText().toString().trim();
            final String serverIp = dialog.getSecondField().getText().toString().trim();
            if (!serverName.isEmpty() && !serverIp.isEmpty()) {
                server.setName(serverName);
                server.setEndpoint(serverIp + (serverIp.endsWith("/") ? "" : "/"));
                serverListAdapter.notifyItemChanged(serverListAdapter.getServers().indexOf(store));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                saveServers(editor);
                editor.apply();
                dialogInterface.dismiss();
            }
        }, getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE);

        handle.setTitle(getString(R.string.dialog_edit_server, server.getName()));
        handle.show();
    }

    private void editTracker(String deviceName, String location, Callback<String> edited) {
        DoubleEditDialog dialog = new DoubleEditDialog(getContext(), getLayoutInflater(),
                new DataField(0, deviceName, getString(R.string.dialog_edit_tracker_name)),
                new DataField(0, location, getString(R.string.dialog_edit_tracker_location)));

        AlertDialog handle = dialog.getDialog();
        handle.setTitle(getString(R.string.dialog_edit_tracker));

        dialog.setButtons(getString(R.string.dialog_save), (dialogInterface, i) -> {
            final String name = dialog.getFirstField().getText().toString();
            final String location1 = dialog.getSecondField().getText().toString();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("scanning_as_tracker", true);
            editor.putString("scan_tracker_name", name);
            editor.putString("scan_tracker_location", location1);
            editor.apply();
            edited.run(name);
        }, getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE);

        handle.show();
    }

    private void deleteServer(Pair<Server, Pair<String, String>> store) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final Server server = store.first;
        new ConfirmDialog(getContext(), getLayoutInflater(), getString(R.string.dialog_confirm, getString(R.string.dialog_confirm_delete)),
                getString(R.string.dialog_delete_confirmation, server.getName()), getString(R.string.dialog_delete), () -> {

            final Fetcher fetcher = Fetcher.getInstance();
            serverListAdapter.notifyItemRemoved(serverListAdapter.getServers().indexOf(store));
            serverListAdapter.getServers().remove(store);
            fetcher.setServerList(serverListAdapter.getServers());
            Pair<Server, Pair<String, String>> next = fetcher.getServerList().get(0);
            if(store.first.isDefaultServer()){
                next.first.setAsDefaultServer(true);
            }
            saveServers(editor);
            editor.apply();
            if (fetcher.getCurrentServer() == server) {
                try {
                    fetcher.init(next.first);
                    fetcher.login(next.second.first, next.second.second, new Callback<RequestError>() {
                        @Override
                        public void run(RequestError data) {
                            if(data == null){
                                Toast.makeText(getContext(), getString(R.string.home_page_server_state) + " " + next.first.getName(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), getString(R.string.login_page_no_connection, data.toString()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (serverListAdapter.getServers().size() <= 0) {
                openLoginActivity();
            }
        });
    }

    private void clearScanLog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int count = sharedPreferences.getStringSet("scan_log", new HashSet<>()).size();
        new ConfirmDialog(getContext(), getLayoutInflater(), getString(R.string.dialog_confirm, getString(R.string.dialog_confirm_clear)),
                getString(R.string.dialog_clear_scan, count), getString(R.string.dialog_clear), () -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("scan_log");
            editor.apply();
            Toast.makeText(getContext(), R.string.settings_scan_history_cleared, Toast.LENGTH_SHORT).show();
        });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.putExtra("autoconnect", false);
        intent.putExtra("embed", true);
        startActivity(intent);
    }

    private void saveServers(SharedPreferences.Editor editor) {
        System.out.println("Saving servers now");
        List<Pair<Server, Pair<String, String>>> serverList = serverListAdapter.getServers();
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

        editor.putStringSet("servers", servers);
        Fetcher.getInstance().setServerList(serverList);
    }

    private void setAutoconnect(SharedPreferences.Editor editor, boolean autoconnect) {
        System.out.println("Saving autoconnect state now");
        editor.putBoolean("autoconnect", autoconnect);
    }

    private boolean isAutoconnectEnabled() {
        return sharedPreferences.getBoolean("autoconnect", true);
    }

    private boolean isScanAsTrackerEnabled() {
        return sharedPreferences.getBoolean("scanning_as_tracker", true);
    }

    private void setRegisterUnknownCodes(SharedPreferences.Editor editor, boolean registerUnknownCodes) {
        System.out.println("Saving registerUnknownCodes state now");
        editor.putBoolean("register_unknown_codes", registerUnknownCodes);
    }

    private boolean canRegisterUnknownCodes() {
        return sharedPreferences.getBoolean("register_unknown_codes", false);
    }

    private void invalidateIntro(SharedPreferences.Editor editor) {
        System.out.println("Saving intro state now");
        editor.putBoolean("iintro", false);
    }

    private void setScanningAsTracker(SharedPreferences.Editor editor, boolean scanningAsTracker) {
        editor.putBoolean("scanning_as_tracker", scanningAsTracker);
        editor.commit();
    }

    private void openSwitchLocale() {
        String[] localeList = getResources().getStringArray(R.array.languages);
        Map<Locale, String> localeMap = new HashMap<>();
        for (int i = 0; i < localeList.length; i++) {
            String localString = localeList[i];
            if (localString.contains("-")) {
                localString = localString.substring(0,
                        localString.indexOf("-"));
            }
            Locale locale = new Locale(localString);
            localeMap.put(locale, locale.getDisplayLanguage() + " ("
                    + localeList[i] + ")");
        }

        List<String> values = new ArrayList<>(localeMap.values());
        String s = localeMap.get(Locale.getDefault());
        System.out.println(Locale.getDefault() + ":" + s);
        final AtomicInteger selectedIndex = new AtomicInteger(values.indexOf(s));
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(R.string.settings_language_dialog_title)
                .setSingleChoiceItems(values.toArray(new String[0]), selectedIndex.get(), (dialogInterface, i) -> selectedIndex.set(i))
                .setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        System.out.println("Selected locale: " + values.get(selectedIndex.get()) + "");
                        //changeServer(Fetcher.getInstance().getServerList().get(selectedIndex.get()));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, DoubleEditDialog.DISPOSE)
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.colorAccent));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.iconAccent));
        });
        dialog.show();
    }

    private void setLocale(Locale locale) {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        configuration.setLocale(locale);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            getContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }
}