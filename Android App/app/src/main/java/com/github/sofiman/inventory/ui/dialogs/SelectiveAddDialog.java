package com.github.sofiman.inventory.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.R;

public class SelectiveAddDialog {

    private final Context context;
    private AlertDialog dialog;

    public SelectiveAddDialog(Context context, LayoutInflater inflater, RecyclerView.Adapter<?> adapter){
        this.context = context;
        final ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.dialog_selective_add, null);

        RecyclerView recyclerView = view.findViewById(R.id.selective_add_list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        TextView title = new TextView(context);
        title.setText(R.string.dialog_selective_add_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(context.getColor(R.color.foreground));
        final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        title.setPadding(px, px, 0, 0);

        dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setCustomTitle(title).create();

        dialog.setView(view);

        dialog.show();
    }

    public void show(){
        dialog.show();
    }

    public Context getContext() {
        return context;
    }
}
