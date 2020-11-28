package com.github.sofiman.inventory.ui.scan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sofiman.inventory.CreateObjectActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.api.ScanLog;
import com.github.sofiman.inventory.impl.HistoryDataModel;
import com.github.sofiman.inventory.model.ScanResultAdapter;
import com.github.sofiman.inventory.ui.dialogs.ConfirmDialog;
import com.github.sofiman.inventory.ui.dialogs.DoubleEditDialog;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScanHistoryActivity extends AppCompatActivity {

    private boolean selecting;
    private List<ScanLog> logs = new ArrayList<>();
    private List<String> logsCache;
    private int currentIdx, logsDone;
    private Callback<ScanLog> scanLogCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        // Set return buttons
        ImageView back = findViewById(R.id.scan_full_back);
        TextView startScanning = findViewById(R.id.scan_full_start_scan);
        back.setOnClickListener(view -> supportFinishAfterTransition());
        startScanning.setOnClickListener(view -> supportFinishAfterTransition());

        // Load scan history
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ScanHistoryActivity.this);
        HistoryDataModel.getInstance().load(sharedPreferences);

        // Add status bar offset (styling)
        ConstraintLayout header = findViewById(R.id.scan_full_layout);
        LayoutHelper.addStatusBarOffset(this, header);

        // Create the scan history list
        RecyclerView recyclerView = findViewById(R.id.scan_full_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ScanResultAdapter adapter = new ScanResultAdapter(this, HistoryDataModel.getInstance().getLog());

        if(adapter.getHistory().size() == 0){
            ConstraintLayout message = findViewById(R.id.scan_full_message);
            message.setVisibility(View.VISIBLE);
        } else {
            // Quick actions
            adapter.setOnSelectListener(new Runnable() {
                @Override
                public void run() {
                    Set<ScanLog> selected = adapter.getSelected();
                    if (selected.size() > 0) {
                        if (!selecting) {
                            startSelecting();
                        }
                        TextView selectedCount = findViewById(R.id.scan_full_selected);
                        selectedCount.setText(String.format(Locale.getDefault(), "%d", selected.size()));
                    } else {
                        stopSelecting();
                    }
                }
            });

            ImageView copy = findViewById(R.id.scan_full_copy);
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int items = adapter.getSelected().size();
                    String str = "";
                    for (ScanLog log : adapter.getSelected()){
                        str += "\n" + log.getContent();
                    }
                    str = str.substring(2);
                    String info = getString(R.string.scan_history_selection_copied,
                            items + " " + getString(R.string.scan_history_selection_one) + (items > 1 ? "s" : ""));
                    StringUtils.setClipboard(ScanHistoryActivity.this, "inventorymanager_history_selection", str);
                    Toast.makeText(ScanHistoryActivity.this, info, Toast.LENGTH_SHORT).show();
                }
            });

            ImageView delete = findViewById(R.id.scan_full_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.getHistory().removeAll(adapter.getSelected());
                    HistoryDataModel.getInstance().setLog(adapter.getHistory());
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ScanHistoryActivity.this);
                    HistoryDataModel.getInstance().save(sharedPreferences);
                    adapter.notifyDataSetChanged();
                    adapter.getSelected().clear();
                    stopSelecting();
                    if(adapter.getHistory().size() == 0){
                        showMessage();
                    }
                }
            });

            ImageView add = findViewById(R.id.scan_full_add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logs.addAll(adapter.getSelected());
                    if(logs.size() > 0){
                        logsCache = new ArrayList<>();
                        for (ScanLog scanLog : logs){
                            logsCache.add(scanLog.getContent());
                        }
                        currentIdx = 0;
                        showCreation(logs.get(0));
                    }
                }
            });
        }
        recyclerView.setAdapter(adapter);
    }

    private void startSelecting(){
        TextView title = findViewById(R.id.scan_full_title);
        title.animate().translationY(0).setDuration(230);

        TextView selectedCount = findViewById(R.id.scan_full_selected);
        selectedCount.setAlpha(0f);
        selectedCount.setScaleY(0);
        selectedCount.setVisibility(View.VISIBLE);
        selectedCount.animate().scaleY(1).alpha(1).setDuration(250);

        ImageView copy = findViewById(R.id.scan_full_copy);
        copy.setAlpha(0f);
        copy.setVisibility(View.VISIBLE);
        copy.animate().alpha(1).setDuration(250);

        ImageView delete = findViewById(R.id.scan_full_delete);
        delete.setAlpha(0f);
        delete.setVisibility(View.VISIBLE);
        delete.animate().alpha(1).setDuration(250);

        ImageView add = findViewById(R.id.scan_full_add);
        add.setAlpha(0f);
        add.setVisibility(View.VISIBLE);
        add.animate().alpha(1).setDuration(250);
        selecting = true;
    }

    private void stopSelecting(){
        TextView title = findViewById(R.id.scan_full_title);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        title.animate().translationY(px).setDuration(175);

        TextView selectedCount = findViewById(R.id.scan_full_selected);
        selectedCount.setVisibility(View.INVISIBLE);

        ImageView copy = findViewById(R.id.scan_full_copy);
        copy.setVisibility(View.GONE);

        ImageView delete = findViewById(R.id.scan_full_delete);
        delete.setVisibility(View.GONE);

        ImageView add = findViewById(R.id.scan_full_add);
        add.setVisibility(View.GONE);
        selecting = false;
    }

    private void showMessage(){
        ConstraintLayout message = findViewById(R.id.scan_full_message);
        message.setVisibility(View.VISIBLE);
    }

    private void hideMessage(){
        ConstraintLayout message = findViewById(R.id.scan_full_message);
        message.setVisibility(View.GONE);
    }

    private void showCreation(ScanLog scanLog) {
        List<Callback<ScanLog>> functions = Arrays.asList(
                this::openInventoryCreation,
                this::openContainerCreation,
                this::openItemCreation
        );
        String[] types = getResources().getStringArray(R.array.types);
        final AtomicInteger selectedIndex = new AtomicInteger(0);
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(R.string.dialog_create_title_all)
                .setSingleChoiceItems(types, selectedIndex.get(), (dialogInterface, i) -> selectedIndex.set(i))
                .setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final int idx = selectedIndex.get();
                        System.out.println("Selected: " + types[idx] + "");
                        scanLogCallback = functions.get(idx);
                        functions.get(idx).run(scanLog);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, DoubleEditDialog.DISPOSE)
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.iconAccent));
        });
        dialog.show();
    }

    private void openInventoryCreation(ScanLog scanLog) {
        startActivityForResult(new IntentBuilder(this, CreateObjectActivity.class)
                .scope("create").type("inventory").blueprint(new Inventory(new byte[0], "", "", scanLog.getLocation(), new ArrayList<>(), new ArrayList<>()))
                .extra(logsCache, logs.indexOf(scanLog))
                .build(), 0);
    }

    private void openContainerCreation(ScanLog scanLog) {
        startActivityForResult(new IntentBuilder(this, CreateObjectActivity.class)
                .scope("create").type("container").blueprint(new Container(new byte[0], "", scanLog.getLocation(), "", "")
                        .setLocations(new ArrayList<>()).setItems(new ArrayList<>()))
                .extra(logsCache, logs.indexOf(scanLog))
                .build(), 1);
    }

    private void openItemCreation(ScanLog scanLog) {
        startActivityForResult(new IntentBuilder(this, CreateObjectActivity.class)
                .scope("create").type("item")
                .blueprint(new Item("", "", "", "", "", "", "",
                        Arrays.asList(new LocationPoint(scanLog.getLocation(), scanLog.getTimestamp())), ""))
                .extra(logsCache, logs.indexOf(scanLog))
                .build(), 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            logsDone++;
            if(logs.size() > logsDone){
                next();
            } else {
                resetState();
            }
        } else if(resultCode == 2){
            resetState();
        }
    }

    private void resetState(){
        logsDone = 0;
        logs.clear();
        logsCache = null;
        scanLogCallback = null;
    }

    private void next(){
        ScanLog scanlog = logs.get(++currentIdx);
        Toast.makeText(this, getString(R.string.create_object_scan_assignment, currentIdx, logs.size()), Toast.LENGTH_SHORT).show();
        scanLogCallback.run(scanlog);
    }
}