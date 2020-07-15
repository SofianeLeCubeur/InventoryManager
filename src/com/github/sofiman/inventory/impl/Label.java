package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.Identifiable;
import com.github.sofiman.inventory.api.models.Inventory;
import com.github.sofiman.inventory.api.models.Item;
import com.github.sofiman.inventory.api.models.Container;
import com.github.sofiman.inventory.utils.ID;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Label {

    private final static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");

    public static BufferedImage drawBigLabel(InventoryManager manager, Identifiable identifiable, String companyName, BufferedImage icon, String additionalInfo) {
        BufferedImage qrImage = manager.printQRCode(identifiable, 180, 180, ErrorCorrectionLevel.H);
        BufferedImage label = new BufferedImage(400, 520, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = label.createGraphics();

        int maxWidth = label.getWidth();
        g.fillRect(0, 0, label.getWidth(), label.getHeight());
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, label.getWidth() - 1, label.getHeight() - 1);
        g.drawImage(qrImage, 10, label.getHeight() - qrImage.getHeight() - 10, null);

        if (identifiable instanceof Item) {
            Item item = (Item) identifiable;
            int y = 0;
            int x = 5;

            g.setFont(g.getFont().deriveFont(32f).deriveFont(Font.PLAIN));
            y = drawStringAndKeepUp(g, companyName, x, y, maxWidth);
            g.setFont(g.getFont().deriveFont(16f).deriveFont(Font.PLAIN));
            y = drawStringAndKeepUp(g, "Powered by Inventory Manager", x, y, maxWidth);
            y += 5;
            g.drawImage(icon, maxWidth - 5 - icon.getWidth(), y / 2 - icon.getHeight() / 2, null);
            g.drawLine(0, y, maxWidth, y);

            g.setFont(g.getFont().deriveFont(32f).deriveFont(Font.PLAIN));
            y += 5;
            y = drawStringAndKeepUp(g, item.getInternalName(), x, y, maxWidth);
            y = drawStringAndKeepUp(g, item.getName(), x, y, maxWidth);
            y = drawStringAndKeepUp(g, item.getDescription(), x, y, maxWidth);
            y = drawStringAndKeepUp(g, item.getSerialNumber(), x, y, maxWidth);
            y += 5;
            g.drawLine(0, y, maxWidth, y);

            x = 10;
            y += 5;
            g.setFont(g.getFont().deriveFont(18f).deriveFont(Font.PLAIN));
            String timestamp = format.format(new Date());
            if(additionalInfo != null && additionalInfo.length() > 0){
                y = drawStringAndKeepUp(g, additionalInfo, x, y, maxWidth);
            }
            y = drawStringAndKeepUp(g, timestamp, x, y, maxWidth);
            y = drawStringAndKeepUp(g, ID.toHex(item.getId()).toUpperCase(), x, y, maxWidth);
            g.setFont(g.getFont().deriveFont(26f).deriveFont(Font.PLAIN));
        }
        // TODO: Do big label for inventories and containers

        return label;
    }

    public static BufferedImage drawSmallLabel(InventoryManager manager, Identifiable identifiable, int size, int pad, boolean showText){
        BufferedImage label = new BufferedImage(size + pad, size + pad, BufferedImage.TYPE_INT_ARGB);
        int qrcodeSize = showText ? size - 40 : size;
        BufferedImage qrImage = manager.printQRCode(identifiable, qrcodeSize, qrcodeSize, ErrorCorrectionLevel.L);
        Graphics2D g = label.createGraphics();
        g.fillRect(0, 0, label.getWidth(), label.getHeight());
        g.drawImage(qrImage, label.getWidth() / 2 - qrImage.getWidth() / 2, label.getHeight() / 2 - qrImage.getHeight() / 2, null);
        if(!showText){
            g.dispose();
            return label;
        }
        g.setColor(Color.decode("#616161"));
        float fontSize = Math.max(size - 100, 0) / 10f;
        System.out.println(fontSize);
        g.setFont(g.getFont().deriveFont(fontSize));
        final int padding = 15;
        String code = identifiable.getCode().toUpperCase();
        String timestamp = format.format(new Date());
        String extra = timestamp;
        if(identifiable instanceof Item){
            extra = ((Item) identifiable).getSerialNumber();
        } else if(identifiable instanceof Inventory){
            extra = ((Inventory) identifiable).getName();
        }

        int x = label.getWidth() / 2 - g.getFontMetrics().stringWidth(code) / 2;
        g.drawString(code, x, padding);

        g.setFont(g.getFont().deriveFont(fontSize + 2));
        g.setTransform(AffineTransform.getRotateInstance(Math.toRadians(-180),
                label.getWidth() / 2f, label.getHeight() / 2f));

        g.drawString(code, x, padding);
        g.setFont(g.getFont().deriveFont(fontSize));
        g.setTransform(AffineTransform.getRotateInstance(Math.toRadians(90),
                label.getWidth() / 2f, label.getHeight() / 2f));

        x = label.getWidth() / 2 - g.getFontMetrics().stringWidth(extra) / 2;
        g.drawString(extra, x, padding);

        g.setTransform(AffineTransform.getRotateInstance(Math.toRadians(-90),
                label.getWidth() / 2f, label.getHeight() / 2f));

        g.setFont(g.getFont().deriveFont(fontSize + 2));
        x = label.getWidth() / 2 - g.getFontMetrics().stringWidth(timestamp) / 2;
        g.drawString(timestamp, x, padding);

        g.dispose();
        return label;
    }

    public static BufferedImage drawMediumLabel(InventoryManager manager, Identifiable identifiable, BufferedImage icon){
        BufferedImage label = new BufferedImage(530, 170, BufferedImage.TYPE_INT_ARGB);
        BufferedImage qrImage = manager.printQRCode(identifiable, 160, 160, ErrorCorrectionLevel.L);
        Graphics2D g = label.createGraphics();
        g.fillRect(0, 0, label.getWidth(), label.getHeight());
        g.drawImage(qrImage, 5, label.getHeight() / 2 - qrImage.getHeight() / 2, null);

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, label.getWidth() - 1, label.getHeight() - 1);
        g.setFont(g.getFont().deriveFont(20f));
        int width = label.getWidth(), height = label.getHeight();
        int y = 5, x = 170;

        g.drawLine(x, 0, x, height);
        x += 10;

        if(identifiable instanceof Item){
            Item item = (Item) identifiable;
            y = drawStringAndKeepUp(g, item.getInternalName(), x, y, width);
            y = drawStringAndKeepUp(g, item.getName(), x, y, width);
            y = drawStringAndKeepUp(g, item.getDescription(), x, y, width);
            y = drawStringAndKeepUp(g, item.getSerialNumber(), x, y, width);
        } else if(identifiable instanceof Container){
            Container container = (Container) identifiable;
            y = drawStringAndKeepUp(g, container.getContentDescription(), x, y, width);
            y = drawStringAndKeepUp(g, container.getDetails(), x, y, width);
            y = drawStringAndKeepUp(g, container.getState(), x, y, width);
            y = drawStringAndKeepUp(g, container.getRegisteredItems().size() + " Items Registered", x, y, width);
        } else if(identifiable instanceof Inventory){
            Inventory inventory = (Inventory) identifiable;
            y = drawStringAndKeepUp(g, inventory.getName(), x, y, width);
            y = drawStringAndKeepUp(g, inventory.getState(), x, y, width);
            y = drawStringAndKeepUp(g, inventory.getItemCount() + " Items Registered", x, y, width);
        }

        y = 110;
        g.drawLine(x - 10, y, width, y);
        y += 5;

        String timestamp = format.format(new Date());
        y = drawStringAndKeepUp(g, timestamp, x, y, width);
        y = drawStringAndKeepUp(g, identifiable.getCode().toUpperCase(), x, y, label.getWidth());

        g.drawImage(icon, width - icon.getWidth() - 3, 3, null);

        return label;
    }

    private static int drawStringAndKeepUp(Graphics2D g, String s, int x, int y, int maxWidth) {
        if (s == null) s = "";
        AttributedString styledText = new AttributedString(s);
        styledText.addAttribute(TextAttribute.FONT, g.getFont());
        AttributedCharacterIterator m_iterator = styledText.getIterator();
        int m_start = m_iterator.getBeginIndex();
        int m_end = m_iterator.getEndIndex();
        FontRenderContext frc = g.getFontRenderContext();

        LineBreakMeasurer measurer = new LineBreakMeasurer(m_iterator, frc);
        measurer.setPosition(m_start);

        int dy = y;
        while (measurer.getPosition() < m_end) {
            TextLayout layout = measurer.nextLayout(maxWidth);

            dy += layout.getAscent();
            float dx = layout.isLeftToRight() ?
                    0 : maxWidth - layout.getAdvance();

            layout.draw(g, x + dx, dy);
            dy += layout.getDescent() + layout.getLeading();
        }
        return dy;
    }
}
