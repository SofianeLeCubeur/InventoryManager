package com.github.sofiman.inventory.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.ui.components.QRCodeHeaderComponent;
import com.github.sofiman.inventory.ui.components.WebhookComponent;
import com.github.sofiman.inventory.utils.StringUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

public class QrCodeGeneratorDialog extends Dialog {

    private Bitmap code;

    public QrCodeGeneratorDialog(Context context, LayoutInflater inflater, Drawable drawable, int[] tints, String name, String content, BarcodeFormat format, boolean imagePadding){
        super(context, inflater);
        QRCodeHeaderComponent header = new QRCodeHeaderComponent(context);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
        header.setPadding(pad, pad, pad, 0);
        ImageView trackerIcon = header.findViewById(R.id.qrcode_header_icon);
        header.setName(name);
        header.setRightDrawableOnClickListener(view -> share());
        header.update();

        trackerIcon.setImageDrawable(drawable);
        if(tints[0] != 0){
            trackerIcon.setImageTintList(ColorStateList.valueOf(tints[0]));
        } else {
            trackerIcon.setImageTintList(null);
        }
        if(tints[1] != 0){
            trackerIcon.setBackgroundTintList(ColorStateList.valueOf(tints[1]));
        } else {
            trackerIcon.setBackgroundTintList(null);
        }
        if(!imagePadding){
            trackerIcon.setPadding(0, 0, 0,0);
        }

        dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_InventoryManager_Dialog)
                .setCustomTitle(header).create();

        final View view = inflater.inflate(R.layout.dialog_qr_code_generator, null);

        TextView qrCodeContent = view.findViewById(R.id.dialog_qr_code_content);
        qrCodeContent.setText(content);
        qrCodeContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringUtils.setClipboard(context, "inventorymanager_qrcode", content);
                Toast.makeText(context, context.getString(R.string.content_copied, "ID"), Toast.LENGTH_SHORT).show();
            }
        });

        ImageView qrCode = view.findViewById(R.id.dialog_qr_code);
        final int size = qrCode.getLayoutParams().width;

        try {
            code = generateCode(content, size, format);
            qrCode.setImageBitmap(code);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        BarcodeFormat[] formats = new BarcodeFormat[]{ BarcodeFormat.QR_CODE, BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX, BarcodeFormat.PDF_417 };
        AtomicInteger pointer = new AtomicInteger(1);
        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(pointer.get() == formats.length){
                        pointer.set(0);
                    }
                    code = generateCode(content, size, formats[pointer.get()]);
                    qrCode.setImageBitmap(code);
                    pointer.incrementAndGet();
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.setView(view);
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                //dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(context.getColor(R.color.aqua));
            }
        });*/
        dialog.show();
    }

    private Bitmap generateCode(String content, int size, BarcodeFormat format) throws WriterException {
        Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
        if(format == BarcodeFormat.QR_CODE){
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // H = 30% damage
            hintMap.put(EncodeHintType.MARGIN, 0);
        }

        MultiFormatWriter writer = new MultiFormatWriter();

        BitMatrix bitMatrix = writer.encode(content, format, size, size, hintMap);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void share(){
        Bitmap bitmap = addWhiteBorder(code, 28);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Inventory Manager Code", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share Code"));
    }

    private static Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }
}
