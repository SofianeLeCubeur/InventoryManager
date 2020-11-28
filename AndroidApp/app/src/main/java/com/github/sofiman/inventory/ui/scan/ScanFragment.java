package com.github.sofiman.inventory.ui.scan;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.github.sofiman.inventory.CreateObjectActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.impl.HistoryDataModel;
import com.github.sofiman.inventory.ui.components.Component;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.ui.components.InventoryComponent;
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.ui.dialogs.ConfirmDialog;
import com.github.sofiman.inventory.ui.dialogs.DoubleEditDialog;
import com.github.sofiman.inventory.utils.Animations;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.gson.Gson;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static android.view.Gravity.CENTER_HORIZONTAL;

public class ScanFragment extends Fragment {

    private Gson gson = new Gson();
    private CodeScanner scanner;
    private String previous = "";
    private AtomicBoolean canScan = new AtomicBoolean(true);
    private boolean addUnknownToHistory = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        if (getActivity() == null) return null;

        CodeScannerView scannerView = view.findViewById(R.id.scanner);
        // Setup camera settings
        scanner = new CodeScanner(getContext(), scannerView);
        scanner.setCamera(CodeScanner.CAMERA_BACK);
        scanner.setFormats(CodeScanner.ALL_FORMATS);
        scanner.setScanMode(ScanMode.CONTINUOUS);
        scanner.setAutoFocusEnabled(true);
        scanner.setTouchFocusEnabled(true);
        scanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                if (!canScan.get()) return;
                canScan.set(false);
                String text = result.getText();
                if (previous.equals(text)) {
                    canScan.set(true);
                    return;
                }
                System.out.println("Successfully scanned: " + text);
                previous = text;
                String id = text.split("_")[0];

                // Fetch animation
                getActivity().runOnUiThread(() -> fetchingState());
                // Send the raw text to the server to fetch it
                Fetcher.getInstance().fetchScanResult(id, HistoryDataModel.getInstance().getScanningLocation(), new APIResponse<HashMap<String, Object>>() {
                    @Override

                    public void response(HashMap<String, Object> callback) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseResult(text, result.getTimestamp(), callback);
                            }
                        });
                        canScan.set(true);
                    }

                    @Override
                    public void error(RequestError error) {
                        System.out.println("Could not connect to the target server: " + error + ", text: " + text);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (error.getError().equals("not_found")) {
                                    showActions(text, result.getTimestamp());
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.login_page_no_connection, error.toString()), Toast.LENGTH_LONG).show();
                                    resetState();
                                }
                                new Handler().postDelayed(() -> previous = "", 500);
                            }
                        });
                        canScan.set(true);
                    }
                });
            }
        });
        scanner.setErrorCallback(error -> {
            canScan.set(false);
            System.out.println("Could not read code: " + error.getMessage());
            error.printStackTrace();
        });

        // Setup the scan canvas
        final RelativeLayout content = view.findViewById(R.id.scan_content);
        ImageView hideContent = view.findViewById(R.id.scan_reduce);
        hideContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hideContent.getVisibility() != View.VISIBLE) {
                    return;
                }
                hideContent.setVisibility(View.INVISIBLE);
                content.setVisibility(View.GONE);
                previous = "";
                /*Animations.collapse(content, new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        hideContent.setVisibility(View.INVISIBLE);
                        previous = "";
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });*/
            }
        });

        // Setup the view more button to open the scan history
        ImageView more = view.findViewById(R.id.scan_more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ScanHistoryActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        contentReset();
        // Ask for permission
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            showConsentDialog();
        } else {
            scanner.startPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check permissions
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanner.startPreview();
            } else {
                showConsentDialog();
            }
        }
    }

    private void showConsentDialog() {
        new ConfirmDialog(getContext(), getLayoutInflater(), R.string.scan_permission_dialog_title,
                R.string.scan_permission_consent, R.string.dialog_accept,
                () -> ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 200));
    }

    @Override
    public void onResume() {
        super.onResume();
        scanner.startPreview();
        this.loadSettings();
        System.out.println("Starting preview");
    }

    @Override
    public void onPause() {
        scanner.releaseResources();
        super.onPause();
        System.out.println("Pausing preview");
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.addUnknownToHistory = sharedPreferences.getBoolean("register_unknown_codes", false);
    }

    /**
     * Resets the activity in a continuous scan mode
     */
    private void resetState() {
        final View view = getView();
        if (view == null) return;
        TextView state = view.findViewById(R.id.scan_state);
        ImageView stateIcon = view.findViewById(R.id.scan_state_icon);
        state.setText(R.string.scan_waiting_for_code);
        stateIcon.setImageResource(R.drawable.qrcode_scan);
    }

    /**
     * Shows fetching animation
     */
    private void fetchingState() {
        final View view = getView();
        if (view == null) return;
        TextView state = view.findViewById(R.id.scan_state);
        ImageView stateIcon = view.findViewById(R.id.scan_state_icon);
        state.setText(R.string.scan_fetching);
        stateIcon.setImageResource(R.drawable.magnify);
    }

    /**
     * Hides the content canvas
     */
    private void contentReset() {
        final View view = getView();
        LinearLayout loading = view.findViewById(R.id.scan_loading);
        loading.setVisibility(View.VISIBLE);
        view.findViewById(R.id.scan_reduce).setVisibility(View.GONE);
        resetState();
    }

    /**
     * Setup the activity to show the scan result
     */
    private void contentLoaded() {
        final View view = getView();
        if (view == null) return;
        view.findViewById(R.id.scan_reduce).setVisibility(View.VISIBLE);
        resetState();
        vibrate();
    }

    private void parseResult(String text, long timestamp, Map<String, Object> obj) {
        contentLoaded();

        // Extract the type from the response
        String type = obj.remove("type").toString();
        String json = gson.toJson(obj);
        obj.remove("success");

        Component view = null;
        Object data = null;
        // Create the required component and populates it with the data
        if (type.equals("inventory")) {
            Inventory store = gson.fromJson(json, Inventory.class);
            InventoryComponent component = new InventoryComponent(getContext());

            component.setName(store.getName());
            component.setIcon(store.getIcon());
            component.setItemCount(store.getItemCount());
            component.setLocation(store.getLocation());
            component.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutHelper.openInventory(getActivity(), Pair.create(store, component));
                }
            });
            data = store;

            view = component;
        } else if (type.equals("container")) {
            Container store = gson.fromJson(json, Container.class);
            ContainerComponent component = new ContainerComponent(getContext());
            System.out.println(json + " -> " + store);

            component.setContent(store.getContent());
            component.setLocation(store.getLocation());
            component.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutHelper.openContainer(getActivity(), Pair.create(store, component));
                }
            });
            data = store;
            view = component;
        } else if (type.equals("item")) {
            Item store = gson.fromJson(json, Item.class);
            ItemComponent component = new ItemComponent(getContext());

            component.setName(store.getName());
            component.setIcon(store.getIcon());
            component.setDescription(store.getDescription());
            component.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutHelper.openItem(getActivity(), Pair.create(store, component));
                }
            });
            data = store;
            view = component;
        }
        HistoryDataModel.getInstance().pushScanLog(text, timestamp, type, data, getContext());

        if (view != null && getView() != null) {
            view.update();

            // Add the component to the canvas
            RelativeLayout content = getView().findViewById(R.id.scan_content);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            content.removeAllViews();
            content.addView(view, params);
            content.setVisibility(View.VISIBLE);
            //Animations.expand(content);
        }
    }

    private void showActions(String scannedText, long timestamp) {
        contentLoaded();

        if (getView() == null) return;
        RelativeLayout content = getView().findViewById(R.id.scan_content);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, +ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = CENTER_HORIZONTAL;

        View v = getLayoutInflater().inflate(R.layout.fragment_scan_actions, null);

        TextView text = v.findViewById(R.id.scan_action_text);
        text.setText(scannedText);

        Button addToHistory = v.findViewById(R.id.scan_action_history);

        if (addUnknownToHistory) {
            HistoryDataModel.getInstance().pushScanLog(scannedText, timestamp, "raw", null, getContext());
            addToHistory.setText(R.string.scan_added_to_history);
            addToHistory.setBackgroundTintList(ColorStateList.valueOf(getContext().getColor(R.color.popup)));
            addToHistory.setTextColor(getContext().getColor(R.color.foreground));
            addToHistory.setEnabled(false);
        } else {
            addToHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HistoryDataModel.getInstance().pushScanLog(scannedText, timestamp, "raw", null, getContext());
                    addToHistory.setText(R.string.scan_added_to_history);
                    addToHistory.setBackgroundTintList(ColorStateList.valueOf(getContext().getColor(R.color.popup)));
                    addToHistory.setTextColor(getContext().getColor(R.color.foreground));
                    addToHistory.setEnabled(false);
                }
            });
        }

        ImageView addItem = v.findViewById(R.id.scan_action_add);
        addItem.setOnClickListener(v1 -> showCreation(scannedText));

        content.removeAllViews();
        content.addView(v, params);
        content.setVisibility(View.VISIBLE);
        //
        // Animations.expand(content);
    }

    private void vibrate() {
        if (getContext() == null) return;
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 29) {
            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
        }
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(300);
        }
    }

    private void showCreation(String scannedText) {
        Runnable[] functions = new Runnable[]{
                () -> openInventoryCreation(scannedText),
                () -> openContainerCreation(scannedText),
                () -> openItemCreation(scannedText)
        };
        String[] types = getResources().getStringArray(R.array.types);
        final AtomicInteger selectedIndex = new AtomicInteger(0);
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(R.string.dialog_create_title)
                .setSingleChoiceItems(types, selectedIndex.get(), (dialogInterface, i) -> selectedIndex.set(i))
                .setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final int idx = selectedIndex.get();
                        functions[idx].run();
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

    private void openInventoryCreation(String scannedText) {
        startActivityForResult(new IntentBuilder(getActivity(), CreateObjectActivity.class)
                .scope("create").type("inventory").blueprint(new Inventory(new byte[0], "", "", "", new ArrayList<>(), new ArrayList<>()))
                .extra(Collections.singletonList(scannedText), 0)
                .build(), 0);
    }

    private void openContainerCreation(String scannedText) {
        startActivityForResult(new IntentBuilder(getActivity(), CreateObjectActivity.class)
                .scope("create").type("container").blueprint(new Container(new byte[0], "", "", "", "")
                        .setLocations(new ArrayList<>()).setItems(new ArrayList<>()))
                .extra(Collections.singletonList(scannedText), 0)
                .build(), 1);
    }

    private void openItemCreation(String scannedText) {
        startActivityForResult(new IntentBuilder(getActivity(), CreateObjectActivity.class)
                .scope("create").type("item")
                .blueprint(new Item("", "", "", "", "", "", "", new ArrayList<>(), ""))
                .extra(Collections.singletonList(scannedText), 0)
                .build(), 2);
    }
}