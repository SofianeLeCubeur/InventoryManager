package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.*;
import com.github.sofiman.inventory.api.models.*;
import com.github.sofiman.inventory.utils.ID;
import com.github.sofiman.inventory.utils.Pair;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.mongodb.internal.HexUtils;
import org.bson.conversions.Bson;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.eq;

public class InventoryManager {

    public static final byte TYPE_INVENTORY = 0;
    public static final byte TYPE_ITEM = 1;
    public static final byte TYPE_CONTAINER = 2;
    public static final byte TYPE_GROUP = 3;
    public static final int ID_LENGTH = 12;

    private final Database database;

    public InventoryManager(Database database) {
        this.database = database;
        this.database.assignManager(this);
        init();
    }

    public Inventory createInventory(String name){
        InventoryImpl iv = new InventoryImpl(ID.generateKeyed(TYPE_INVENTORY, ID_LENGTH), name);
        iv.database = database;
        database.register(iv);
        return iv;
    }

    public Inventory createInventory(String name, String location, String state){
        InventoryImpl iv = new InventoryImpl(ID.generateKeyed(TYPE_INVENTORY, ID_LENGTH), name);
        iv.setLocation(location);
        iv.setState(state);
        iv.database = database;
        database.register(iv);
        return iv;
    }

    public Inventory createUnregisteredInventory(byte[] id, String name, String location, String state, Set<byte[]> items) {
        id[0] = TYPE_INVENTORY;
        InventoryImpl inventory = new InventoryImpl(id, name);
        inventory.setState(state);
        inventory.setLocation(location);
        inventory.setItems(items);
        inventory.database = database;
        return inventory;
    }

    public Inventory getInventory(byte[] id){
        if(id[0] != TYPE_INVENTORY) throw new IllegalArgumentException("The provided ID does not have the INVENTORY key");
        return database.findInventory(ID_BIN_FILTER.apply(id));
    }

    public Item createUnregisteredItem(byte[] id, String name, String internalName, String serialNumber, String description, String state,
                                       String details, List<LocationPoint> locationHistory, List<Tag> tags, byte[] parentId){
        id[0] = TYPE_ITEM;
        ItemImpl item = new ItemImpl(id, name, internalName, serialNumber, description);
        item.setState(state);
        item.setDetails(details);
        item.setLocationHistory(locationHistory);
        item.setTags(tags);
        item.setParentId(parentId);
        item.database = database;
        return item;
    }

    public Item registerItem(String name, String internalName, String serialNumber, String description){
        ItemImpl item = new ItemImpl(ID.generateKeyed(TYPE_ITEM, ID_LENGTH), name, internalName, serialNumber, description);
        item.database = database;
        database.register(item);
        return item;
    }

    public Item registerItem(String name, String internalName, String serialNumber, String description, String state,
                              String details, List<LocationPoint> locationHistory, List<Tag> tags){
        ItemImpl item = new ItemImpl(ID.generateKeyed(TYPE_ITEM, ID_LENGTH), name, internalName, serialNumber, description);
        item.setState(state);
        item.setDetails(details);
        item.setLocationHistory(locationHistory);
        item.setTags(tags);
        item.database = database;
        database.register(item);
        return item;
    }

    public Item getItem(byte[] id){
        if(id[0] != TYPE_ITEM) throw new IllegalArgumentException("The provided ID does not have the ITEM key");
        return database.findItem(ID_BIN_FILTER.apply(id));
    }

    /**
     * Creates a registered Container (synced with the database)
     * @param contentDescription Container's content description
     * @return a new Container
     */
    public Container createContainer(String contentDescription){
        ContainerImpl c = new ContainerImpl(ID.generateKeyed(TYPE_CONTAINER, ID_LENGTH), contentDescription);
        c.database = database;
        database.register(c);
        return c;
    }

    /**
     * Create a registered Container (synced with the database)
     * @param contentDescription Container's content Description
     * @param details Container's details
     * @param state Container's state
     * @param locationHistory Container's location history
     * @param registeredItems Container's items
     * @return a new Container
     */
    public Container createContainer(String contentDescription, String details, String state, List<LocationPoint> locationHistory, Map<Long, byte[]> registeredItems){
        ContainerImpl c = new ContainerImpl(ID.generateKeyed(TYPE_CONTAINER, ID_LENGTH), contentDescription);
        c.setDetails(details);
        c.setState(state);
        c.setLocationHistory(locationHistory);
        c.setRegisteredItems(registeredItems);
        c.database = database;
        database.register(c);
        return c;
    }

    /**
     * Create a unregistered Container (not synced with the database)
     * @param id Container's ID
     * @param contentDescription Container's content Description
     * @param details Container's details
     * @param state Container's state
     * @param locationHistory Container's location history
     * @param registeredItems Container's items
     * @return a new Container
     */
    public Container createUnregisteredContainer(byte[] id, String contentDescription, String details, String state, List<LocationPoint> locationHistory, Map<Long, byte[]> registeredItems){
        id[0] = TYPE_CONTAINER; // Overrides the ID's Key
        ContainerImpl c = new ContainerImpl(id, contentDescription);
        c.setDetails(details);
        c.setState(state);
        c.setLocationHistory(locationHistory);
        c.setRegisteredItems(registeredItems);
        c.database = database;
        return c;
    }

    /**
     * Retrieve the container with the specified ID
     * @param id Container's ID
     * @return The container, is it doesn't exists, null
     */
    public Container getContainer(byte[] id){
        if(id[0] != TYPE_CONTAINER) throw new IllegalArgumentException("The provided ID does not have the CONTAINER key");
        return database.findContainer(ID_BIN_FILTER.apply(id));
    }

    /**
     * Scan any QR Code containing a Keyed Object ID (first byte has a signification).
     * Warning: If the
     * A database query is used to retrieve the object information.
     * Supported Objects: Items, Containers, Inventories
     * @param deviceName The device where the code was scanned
     * @param location The location of the device
     * @param code The image to scan for QR Codes
     * @return the object if the scanned code is valid else null
     */
    public Pair<Identifiable, Integer> scan(String deviceName, String location, BufferedImage code){
        try {
            // Scan image for QR Codes
            LuminanceSource source = new BufferedImageLuminanceSource(code);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap); // Retrieve the QR Code content

            // Extract the Recipient ID and Recipient Date from the raw text
            String data = result.getText();
            int und = data.indexOf("_");
            if(und == -1) und = data.length(); // If the date was not written then ignore it
            String date = data.substring(und+1); // Raw Recipient Date
            long timestamp = -1;
            try {
                timestamp = Long.parseLong(date); // Try to parse the recipient in an long format
            } catch (Exception ignored){
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
                try {
                    timestamp = format.parse(date).getTime(); // Try to parse the recipient in a string format
                } catch (Exception ignored2){
                }
            }
            String hex = data.substring(0, und); // Raw Recipient ID
            Identifiable identifiable = null;
            String typeName = "<?>";
            int type = Integer.parseInt(hex.substring(0, 2), 16); // Retrieve the key from the ID

            switch(type){
                case TYPE_INVENTORY:
                    identifiable = database.findInventory(ID_STR_FILTER.apply(hex));
                    typeName = "Inventory";
                    break;
                case TYPE_ITEM:
                    identifiable = database.findItem(ID_STR_FILTER.apply(hex));
                    typeName = "Item";
                    break;
                case TYPE_CONTAINER:
                    identifiable = database.findContainer(ID_STR_FILTER.apply(hex));
                    typeName = "Container";
                    break;
                default:
                    // Generate a dummy identifiable object if the object is not in a known form
                    identifiable = new Identifiable() {
                        @Override
                        public byte[] getId() {
                            return ID.fromHex(hex);
                        }

                        @Override
                        public String getCode() {
                            return hex;
                        }
                    };
                    break;
            }

            System.out.println("Scannable found with " + deviceName + " (in " + location + ") " +
                    typeName + " with ID: " + hex + (timestamp != -1 ? " (Generated on " + timestamp + ")" : ""));

            // Keep log of all the on-going scans
            database.pushScanLog(new ScanLogImpl(deviceName, location, System.currentTimeMillis(), hex, timestamp));
            return new Pair<>(identifiable, type);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to find or scan the QR Code: " + e.getMessage());
        }
        return null;
    }

    /**
     * Generates a QR Code coresponding to the specified object
     * @param identifiable The QR Code recipient
     * @param width Width of the generated QR Code
     * @param height Height of the generated QR Code
     * @return The freshly generated QR Code
     */
    public BufferedImage printQRCode(Identifiable identifiable, int width, int height, ErrorCorrectionLevel errorCorrectionLevel){
        String code = identifiable.getCode();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.MARGIN, 0);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            long date = System.currentTimeMillis();
            // The QR Code content is in the form of: <Recipient ID>_<Recipient Date>
            // Where the Recipient Date is the date of creation of the QR Code
            BitMatrix bitMatrix = writer.encode(code + "_" + date, BarcodeFormat.QR_CODE, width, height, hints);

            // Generate the QR Code with the xzing lib
            return MatrixToImageWriter.toBufferedImage(bitMatrix,
                    new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update all provided objects through the database adpater
     * @param identifiables Objects to update
     * @return InventoryManager
     */
    public InventoryManager update(Identifiable... identifiables){
        for(Identifiable identifiable : identifiables){
            if(identifiable instanceof Item){
                database.update((Item) identifiable);
            } else if(identifiable instanceof Container){
                database.update((Container) identifiable);
            } else if(identifiable instanceof Inventory){
                database.update((Inventory) identifiable);
            }
        }
        return this;
    }

    public List<ScanLog> queryScanLog(Query filter){
        return database.queryScanLog(filter);
    }

    public List<ScanLog> queryScanLog(Query filter, int offset, int length){
        return database.queryScanLog(filter, offset, length);
    }

    /**
     * Stop the database connection
     */
    public void stop(){
        this.database.disconnect();
    }

    /**
     * Start the database connection
     */
    public void init(){
        this.database.connect();
    }

    public static final Function<Identifiable, Bson> ID_FILTER = o -> eq("_id", o.getCode());
    public static final Function<byte[], Bson> ID_BIN_FILTER = o -> eq("_id", HexUtils.toHex(o));
    public static final Function<String, Bson> ID_STR_FILTER = o -> eq("_id", o);
}
