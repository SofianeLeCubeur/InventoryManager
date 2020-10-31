package com.github.sofiman.inventory.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.github.sofiman.inventory.InventoryActivity;
import com.github.sofiman.inventory.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {

    public static String limit(String str, int maxLength) {
        String s = str;
        if (str.length() > maxLength) {
            s = s.substring(0, maxLength - 3) + "...";
        }
        return s;
    }

    public static View.OnLongClickListener createLongClickCopy(Context context, String data, @StringRes int content) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                StringUtils.setClipboard(context, "inventorymanager_qrcode", data);
                Toast.makeText(context, context.getString(R.string.content_copied, context.getString(content)), Toast.LENGTH_SHORT).show();
                return true;
            }
        };
    }

    public static void setClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    public static String formatDate(Context context, long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        long days = (System.currentTimeMillis() - timestamp) / 86400000;
        if (days == 0) {
            return context.getString(R.string.date_today_at, format.format(timestamp));
        } else {
            return context.getString(R.string.date_ago, days, format.format(timestamp));
        }
    }
}
