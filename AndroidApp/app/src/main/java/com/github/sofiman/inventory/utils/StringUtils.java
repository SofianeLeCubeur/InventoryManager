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

    public static void setClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    public static String formatDate(Context context, long timestamp) {
        final long dayPerMillis = 86400000;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        long days = (System.currentTimeMillis() - timestamp) / dayPerMillis;
        if (days == 0) {
            long d1 = System.currentTimeMillis() / dayPerMillis;
            long d2 = timestamp / dayPerMillis;
            if (d1 == d2) {
                return context.getString(R.string.date_today_at, format.format(timestamp));
            } else {
                return context.getString(R.string.date_yesterday_at, format.format(timestamp));
            }
        } else {
            return context.getString(R.string.date_ago, days, format.format(timestamp));
        }
    }
}
