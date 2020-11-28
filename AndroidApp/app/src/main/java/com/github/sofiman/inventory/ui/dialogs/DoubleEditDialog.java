package com.github.sofiman.inventory.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.DataField;

public class DoubleEditDialog extends Dialog {

    private final EditText firstField;
    private final EditText secondField;

    public DoubleEditDialog(Context context, LayoutInflater inflater, DataField first, DataField second) {
        super(context, inflater);
        final View view = inflater.inflate(R.layout.dialog_edit_double, null);

        final TextView firstLabel = view.findViewById(R.id.double_edit_dialog_first_label);
        final TextView secondLabel = view.findViewById(R.id.double_edit_dialog_second_label);
        firstField = (EditText) view.findViewById(R.id.double_edit_dialog_first_field);
        secondField = (EditText) view.findViewById(R.id.double_edit_dialog_second_field);

        firstLabel.setText(first.getHint());
        firstField.setText(first.getValue());

        secondLabel.setText(second.getHint());
        secondField.setText(second.getValue());

        dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(R.string.dialog_edit).create();

        dialog.setView(view);
    }

    public DoubleEditDialog setButtons(String positiveButton, DialogInterface.OnClickListener positiveCallback, String negativeButton, DialogInterface.OnClickListener negativeCallback){
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButton, negativeCallback);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveButton, positiveCallback);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.green));
            }
        });

        return this;
    }

    public EditText getFirstField() {
        return firstField;
    }

    public EditText getSecondField() {
        return secondField;
    }

    public static final DialogInterface.OnClickListener DISPOSE = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };
}
