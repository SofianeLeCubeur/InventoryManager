package com.github.sofiman.inventory.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import androidx.annotation.StringRes;

import com.github.sofiman.inventory.R;

public class ConfirmDialog extends Dialog {

    public ConfirmDialog(Context context, LayoutInflater inflater, String title, String message, String positiveMessage, Runnable confirmed) {
        super(context, inflater);
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(title).setMessage(message)
                .setPositiveButton(positiveMessage, (dialogInterface, i) -> {
                    confirmed.run();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE).create();

        alertDialog.setOnShowListener(dialogInterface -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorAccent)));

        alertDialog.show();
    }

    public ConfirmDialog(Context context, LayoutInflater inflater, @StringRes int title, @StringRes int message, @StringRes int positiveMessage, Runnable confirmed) {
        super(context, inflater);
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(title).setMessage(message)
                .setPositiveButton(positiveMessage, (dialogInterface, i) -> {
                    confirmed.run();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE).create();

        alertDialog.setOnShowListener(dialogInterface -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorAccent)));

        alertDialog.show();
    }

    public ConfirmDialog(Context context, LayoutInflater inflater, @StringRes int title, @StringRes int message,
                         @StringRes int positiveMessage, @StringRes int negativeMessage, @StringRes int neutralMessage, Runnable confirmed, Runnable neutral, Runnable negative) {
        super(context, inflater);
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(title).setMessage(message)
                .setPositiveButton(positiveMessage, (dialogInterface, i) -> {
                    confirmed.run();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(negativeMessage, (dialogInterface, which) -> {
                    negative.run();
                    dialogInterface.dismiss();
                })
                .setNeutralButton(neutralMessage, (dialogInterface, which) -> {
                    neutral.run();
                    dialogInterface.dismiss();
                }).create();

        alertDialog.setOnShowListener(dialogInterface -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorAccent)));

        alertDialog.show();
    }
}
