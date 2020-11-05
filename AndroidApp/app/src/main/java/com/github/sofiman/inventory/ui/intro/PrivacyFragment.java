package com.github.sofiman.inventory.ui.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.ui.dialogs.ConfirmDialog;
import com.github.sofiman.inventory.ui.login.LoginActivity;
import com.github.sofiman.inventory.utils.LayoutHelper;

public class PrivacyFragment extends Fragment {

    private final String ip = "http://46.182.6.127/im/v1/api";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int statusBarHeight = LayoutHelper.getStatusBarHeight(getContext());
        LinearLayout header = view.findViewById(R.id.privacy_header);
        header.getLayoutParams().height += statusBarHeight;
        LayoutHelper.addStatusBarOffset(getContext(), header);

        TextView join = view.findViewById(R.id.privacy_join);
        TextView skip = view.findViewById(R.id.privacy_skip);

        join.setOnClickListener(v -> showConfirmDialog());

        skip.setOnClickListener(v -> {
            disableIntro();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });
    }

    private void showConfirmDialog(){
        new ConfirmDialog(getContext(), getLayoutInflater(), R.string.privacy_join_dialog, R.string.privacy_join_dialog_desc, R.string.privacy_join_dialog_login,
                R.string.privacy_join_dialog_register, R.string.dialog_cancel, () -> {
                    disableIntro();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.putExtra("embed", true);
                    intent.putExtra("autoconnect", false);
                    intent.putExtra("action", R.string.login_page_title);
                    intent.putExtra("server", ip);
                    startActivity(intent);
                }, () -> {
        }, () -> {
            disableIntro();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.putExtra("embed", true);
            intent.putExtra("autoconnect", false);
            intent.putExtra("action", R.string.login_page_register);
            intent.putExtra("server", ip);
            intent.putExtra("confirm_password", true);
            startActivity(intent);
        });
    }

    private void disableIntro(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.edit().putBoolean("iintro", true).apply();
        System.out.println("Intro disabled");
    }
}