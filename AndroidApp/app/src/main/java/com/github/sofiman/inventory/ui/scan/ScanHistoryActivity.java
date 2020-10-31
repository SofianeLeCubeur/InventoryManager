package com.github.sofiman.inventory.ui.scan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.ScanLog;
import com.github.sofiman.inventory.impl.HistoryDataModel;
import com.github.sofiman.inventory.model.ScanResultAdapter;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.StringUtils;

import java.util.Locale;
import java.util.Set;

public class ScanHistoryActivity extends AppCompatActivity {

    private boolean selecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        // Set return buttons
        ImageView back = findViewById(R.id.scan_full_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });
        TextView startScanning = findViewById(R.id.scan_full_start_scan);
        startScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });

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
                    String info = getString(R.string.scan_history_selection_copied, items + " item" + (items > 1 ? "s" : ""));
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
}