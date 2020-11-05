package com.github.sofiman.inventory.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

public abstract class Dialog {

    protected final Context context;
    protected AlertDialog dialog;
    protected final LayoutInflater inflater;

    protected Dialog(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public Context getContext() {
        return context;
    }

    public void show(){
        dialog.show();
    }
}
