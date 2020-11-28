package com.github.sofiman.inventory.ui.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.utils.Callback;

public class EditLocationDialog extends Dialog {

    public EditLocationDialog(Activity context, LayoutInflater inflater, Callback<LocationPoint> callback) {
        super(context, inflater);
        final View view = inflater.inflate(R.layout.dialog_edit_location, null);

        EditText label = view.findViewById(R.id.dialog_edit_location_label);
        EditText latitude = view.findViewById(R.id.dialog_edit_geo_latitude);
        EditText longitude = view.findViewById(R.id.dialog_edit_geo_longitude);

        Button locate = view.findViewById(R.id.dialog_edit_locate);
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acquirePosition(latitude, longitude);
            }
        });

        dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(R.string.dialog_edit_location_title).create();

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocationPoint point = new LocationPoint(label.getText().toString(), System.currentTimeMillis());
                try {
                    double lat = Double.parseDouble(latitude.getText().toString());
                    double lon = Double.parseDouble(longitude.getText().toString());
                    point.setLatitude(lat);
                    point.setLongitude(lon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callback.run(point);
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.green));
            }
        });

        dialog.setView(view);
    }

    private void acquirePosition(EditText latitude, EditText longitude) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Cannot locate: permission denied.");
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latitude.setText(String.valueOf(location.getLatitude()));
            longitude.setText(String.valueOf(location.getLongitude()));
            System.out.println("Successfully got location: " + location);
        }
    }
}
