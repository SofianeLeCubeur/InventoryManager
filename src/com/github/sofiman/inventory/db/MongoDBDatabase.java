package com.github.sofiman.inventory.db;

import com.github.sofiman.inventory.api.*;
import com.github.sofiman.inventory.api.models.*;
import com.github.sofiman.inventory.api.models.Container;
import com.github.sofiman.inventory.impl.InventoryManager;
import com.github.sofiman.inventory.impl.ScanLogImpl;
import com.github.sofiman.inventory.utils.ID;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.*;
import org.bson.conversions.Bson;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;

public class MongoDBDatabase implements Database {

    private InventoryManager manager;
    private final String url;
    private final String dbName;
    private MongoClient client;
    private MongoDatabase db;
    private final long itemCacheExpire;
    private final boolean saveChanges;

    public MongoDBDatabase(String url, String dbName){
        this(url, dbName, 3 * 60 * 1000, true);
    }

    public MongoDBDatabase(String url, String dbName, long itemCacheExpire, boolean saveChanges){
        this.url = url;
        this.dbName = dbName;
        this.itemCacheExpire = itemCacheExpire;
        this.saveChanges = saveChanges;
    }

    @Override
    public void assignManager(InventoryManager manager) {
        this.manager = manager;
    }

    @Override
    public void update(Item item) {
        MongoCollection<Document> collection = db.getCollection("items");
        Bson filter = eq("_id", new BsonString(item.getCode()));
        BasicDBObject command = new BasicDBObject();
        command.put("$set", itemToBson(item));
        collection.updateOne(filter, command);
    }

    @Override
    public void update(Container container) {
        MongoCollection<Document> collection = db.getCollection("containers");
        Bson filter = eq("_id", container.getCode());
        BasicDBObject command = new BasicDBObject();
        command.put("$set", containerToBson(container));
        collection.updateOne(filter, command);
    }

    @Override
    public void update(Inventory inventory) {
        MongoCollection<Document> collection = db.getCollection("inventories");
        Bson filter = eq("_id", inventory.getCode());
        BasicDBObject command = new BasicDBObject();
        command.put("$set", inventoryToBson(inventory));
        collection.updateOne(filter, command);
    }

    @Override
    public void register(Item item) {
        MongoCollection<Document> collection = db.getCollection("items");
        collection.insertOne(itemToBson(item));
    }

    @Override
    public void register(Container container) {
        MongoCollection<Document> collection = db.getCollection("containers");
        collection.insertOne(containerToBson(container));
    }

    @Override
    public void register(Inventory inventory) {
        MongoCollection<Document> collection = db.getCollection("inventories");
        collection.insertOne(inventoryToBson(inventory));
    }

    @Override
    public void remove(Item item) {
        MongoCollection<Document> collection = db.getCollection("items");
        collection.deleteOne(eq("_id", item.getCode()));
    }

    @Override
    public void remove(Container container) {
        MongoCollection<Document> collection = db.getCollection("containers");
        collection.deleteOne(eq("_id", container.getCode()));
    }

    @Override
    public void remove(Inventory inventory) {
        MongoCollection<Document> collection = db.getCollection("inventories");
        collection.deleteOne(eq("_id", inventory.getCode()));
    }

    @Override
    public Item findItem(Bson filter) {
        MongoCollection<Document> collection = db.getCollection("items");
        FindIterable<Document> matches = collection.find(filter);
        return bsonToItem(matches.cursor().next());
    }

    @Override
    public List<Item> findItems(Bson filter) {
        MongoCollection<Document> collection = db.getCollection("items");
        FindIterable<Document> matches = collection.find(filter);
        List<Item> items = new ArrayList<>();
        matches.forEach((Consumer<? super Document>) document -> {
            try {
                items.add(bsonToItem(document));
            } catch (Exception ignored){
            }
        });
        return items;
    }

    @Override
    public Container findContainer(Bson filter) {
        MongoCollection<Document> collection = db.getCollection("containers");
        FindIterable<Document> matches = collection.find(filter);
        return bsonToContainer(matches.cursor().next());
    }

    @Override
    public Inventory findInventory(Bson filter) {
        MongoCollection<Document> collection = db.getCollection("inventories");
        FindIterable<Document> matches = collection.find(filter);
        return bsonToInventory(matches.cursor().next());
    }

    @Override
    public void pushScanLog(ScanLog log) {
        MongoCollection<Document> collection = db.getCollection("scan_log");
        Document document = new Document();
        document.append("device", log.getDevice());
        document.append("location", log.getLocation());
        document.append("timestamp", log.getTimestamp());
        Document recipient = new Document();
        recipient.append("id", log.getRecipient());
        recipient.append("creation", log.getRecipientDate());
        document.append("recipient", recipient);
        collection.insertOne(document);
    }

    private ScanLog documentToScanLog(Document document){
        String device = document.getString("device");
        String location = document.getString("location");
        long timestamp = document.getLong("timestamp");
        Document recipient = (Document) document.get("recipient");
        String recipientId = recipient.getString("id");
        long recipientDate = recipient.getLong("creation");

        return new ScanLogImpl(device, location, timestamp, recipientId, recipientDate);
    }

    @Override
    public List<ScanLog> queryScanLog(Query filter, int offset, int length) {
        MongoCollection<Document> collection = db.getCollection("scan_log");
        FindIterable<Document> matches = collection.find(filter.toBson()).skip(offset).limit(length);
        List<ScanLog> logs = new ArrayList<>();
        matches.forEach((Consumer<? super Document>) document -> {
            try {
                logs.add(documentToScanLog(document));
            } catch (Exception ignored){
            }
        });
        return logs;
    }

    @Override
    public List<ScanLog> queryScanLog(Query filter) {
        MongoCollection<Document> collection = db.getCollection("scan_log");
        FindIterable<Document> matches = collection.find(filter.toBson());
        List<ScanLog> logs = new ArrayList<>();
        matches.forEach((Consumer<? super Document>) document -> {
            try {
                logs.add(documentToScanLog(document));
            } catch (Exception ignored){
            }
        });
        return logs;
    }

    @Override
    public void connect() {
        this.client = MongoClients.create(this.url);
        this.db = client.getDatabase(this.dbName);
    }

    @Override
    public void disconnect() {
        this.client.close();
    }

    @Override
    public long getItemCacheExpire() {
        return itemCacheExpire;
    }

    @Override
    public boolean saveChanges() {
        return saveChanges;
    }

    private Document itemToBson(Item item){
        BsonDocument b;
        Document document = new Document();
        document.append("_id", item.getCode());
        document.append("name", item.getName());
        document.append("reference", item.getInternalName());
        document.append("serial_number", item.getSerialNumber());
        document.append("description", item.getDescription());
        document.append("state", item.getState());
        BsonArray locations = new BsonArray();
        for(LocationPoint point : item.getLocationHistory()){
            b = new BsonDocument();
            b.append("timestamp", new BsonInt64(point.getTimestamp()));
            b.append("location", new BsonString(point.getLocation()));
            locations.add(b);
        }
        document.append("locations", locations);
        document.append("details", item.getDetails());
        BsonArray tags = new BsonArray();
        for(Tag t : item.getTags()){
            b = new BsonDocument();
            b.append("id", new BsonString(t.getId()));
            b.append("label", new BsonString(t.getLabel()));
            String color = "#" + Integer.toHexString(t.getColor().getRGB()).substring(2);
            b.append("cc", new BsonString(color));
            tags.add(b);
        }
        document.append("tags", tags);
        if(item.getParent() != null){
            document.append("parent", item.getParent().getId());
        }

        return document;
    }

    private Item bsonToItem(Document document){
        if(document == null) return null;
        String code = document.getString("_id");
        byte[] id = ID.fromHex(code);
        String name = document.getString("name");
        String reference = document.getString("reference");
        String serial_number = document.getString("serial_number");
        String description = document.getString("description");
        String state = document.getString("state");
        String details = document.getString("details");
        List<LocationPoint> locationHistory = new ArrayList<>();
        ArrayList<Document> locations = (ArrayList<Document>) document.get("locations");
        if(locations.size() > 0){
            long timestamp;
            String location;
            for(Document b : locations){
                timestamp = b.getLong("timestamp");
                location = b.getString("location");
                locationHistory.add(new LocationPoint(timestamp, location));
            }
        }

        List<Tag> itemTags = new ArrayList<>();
        ArrayList<Document> tags = (ArrayList<Document>) document.get("tags");
        String tid, label, color;
        for(Document b : tags){
            tid = b.getString("id");
            label = b.getString("label");
            color = b.getString("cc");

            itemTags.add(new Tag(tid, label, Color.decode(color)));
        }
        byte[] parentId = null;
        if(document.containsKey("parent")){
            parentId = ID.fromHex(document.getString("parent"));
        }
        return this.manager.createUnregisteredItem(id, name, reference, serial_number, description, state, details, locationHistory, itemTags, parentId);
    }

    private Document containerToBson(Container container){
        Document document = new Document();
        document.append("_id", container.getCode());
        document.append("content", container.getContentDescription());
        document.append("details", container.getDetails());
        BsonArray locations = new BsonArray();
        BsonDocument b;
        for(LocationPoint point : container.getLocationHistory()){
            b = new BsonDocument();
            b.append("timestamp", new BsonInt64(point.getTimestamp()));
            b.append("location", new BsonString(point.getLocation()));
            locations.add(b);
        }
        document.append("locations", locations); // TODO: Continue container serialization and add location history to both containers and items
        document.append("state", container.getState());
        BsonArray items = new BsonArray();
        for(Map.Entry<Long, byte[]> item : container.getRegisteredItems().entrySet()){
            b = new BsonDocument();
            b.append("id", new BsonString(ID.toHex(item.getValue())));
            b.append("registered_on", new BsonInt64(item.getKey()));
            items.add(b);
        }
        document.append("items", items);
        if(container.getParent() != null){
            document.append("parent", container.getParent().getId());
        }

        return document;
    }

    private Container bsonToContainer(Document document){
        String code = document.getString("_id");
        byte[] id = ID.fromHex(code);
        String content = document.getString("content");
        String details = document.getString("details");
        ArrayList<Document> locations = (ArrayList<Document>) document.get("locations");
        List<LocationPoint> locationHistory = new ArrayList<>();
        String location;
        long timestamp;
        for(Document b : locations){
            timestamp = b.getLong("timestamp");
            location = b.getString("location");
            locationHistory.add(new LocationPoint(timestamp, location));
        }
        String state = document.getString("state");
        ArrayList<Document> items = (ArrayList<Document>) document.get("items");
        Map<Long, byte[]> registeredItems = new HashMap<>();
        for(Document b : items){
            timestamp = b.getLong("registered_on");
            code = b.getString("id");
            registeredItems.put(timestamp, ID.fromHex(code));
        }

        return this.manager.createUnregisteredContainer(id, content, details, state, locationHistory, registeredItems);
    }

    private Document inventoryToBson(Inventory inventory){
        Document document = new Document();
        document.append("_id", inventory.getCode());
        document.append("name", inventory.getName());
        document.append("location", inventory.getLocation());
        document.append("state", inventory.getState());
        BsonArray items = new BsonArray();
        Set<byte[]> ids = inventory.getRegisteredItemIds();
        for(byte[] item : ids){
            items.add(new BsonString(ID.toHex(item)));
        }
        document.append("items", items);

        return document;
    }

    private Inventory bsonToInventory(Document document){
        String code = document.getString("_id");
        byte[] id = ID.fromHex(code);
        String name = document.getString("name");
        String location = document.getString("location");
        String state = document.getString("state");
        ArrayList<String> items = (ArrayList<String>) document.get("items");
        Set<byte[]> registeredItems = new HashSet<>();
        for (String b : items){
            registeredItems.add(ID.fromHex(b));
        }
        return this.manager.createUnregisteredInventory(id, name, location, state, registeredItems);
    }

}
