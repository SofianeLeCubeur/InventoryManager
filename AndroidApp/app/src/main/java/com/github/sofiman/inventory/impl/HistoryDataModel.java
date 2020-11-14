package com.github.sofiman.inventory.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.ScanLog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class HistoryDataModel {

    private static final HistoryDataModel instance = new HistoryDataModel();

    public static HistoryDataModel getInstance() {
        return instance;
    }

    private Gson gson = new Gson();
    private String scanningLocation;
    private LinkedList<ScanLog> log;
    private LinkedList<SearchHistoryItem> searchHistory;
    private int limit = 5;

    public HistoryDataModel() {
        this.log = new LinkedList<>();
        this.searchHistory = new LinkedList<>();
        this.scanningLocation = "";
    }

    public ScanLog pushScanLog(String scanText, long timestamp, String type, Object object, Context saveContext) {
        ScanLog result = new ScanLog(getDeviceName(), scanningLocation, scanText, timestamp).setRecipient(object, type);
        log.push(result);

        if(saveContext != null){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(saveContext);
            save(sharedPreferences);
        }

        return result;
    }

    public String getScanningLocation() {
        return scanningLocation;
    }

    public void pushSearchEntry(SearchHistoryItem historyItem, Context saveContext){
        this.searchHistory.addFirst(historyItem);
        if(this.searchHistory.size() > limit){
            this.searchHistory.removeLast();
        }

        if(saveContext != null){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(saveContext);
            save(sharedPreferences);
        }
    }

    public void setSearchHistoryCapacity(int limit) {
        this.limit = limit;
    }

    public int getSearchHistoryCapacity() {
        return limit;
    }

    public LinkedList<ScanLog> getLog() {
        return log;
    }

    public void setLog(LinkedList<ScanLog> log) {
        this.log = log;
    }

    public LinkedList<SearchHistoryItem> getSearchHistory() {
        return searchHistory;
    }

    public void setSearchHistory(LinkedList<SearchHistoryItem> searchHistory) {
        this.searchHistory = searchHistory;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    public void save(SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("scan_tracker_location", this.scanningLocation);

        String json;

        Set<String> rawLog = new HashSet<>();
        for (ScanLog log : log) {
            json = gson.toJson(log);
            rawLog.add(json);
        }
        editor.putStringSet("scan_log", rawLog);

        System.out.println("Saving scan history now (" + rawLog.size() + " items)");

        Set<String> rawSearchHistory = new HashSet<>();
        for(SearchHistoryItem item : searchHistory){
            json = gson.toJson(item);
            rawSearchHistory.add(json);
        }

        editor.putStringSet("search_history", rawSearchHistory);
        System.out.println("Saving search history now (" + rawSearchHistory.size() + " items)");

        editor.apply();
    }

    public void load(SharedPreferences sharedPreferences){
        this.log.clear();
        if(sharedPreferences.getBoolean("scanning_as_tracker", false)){
            this.scanningLocation = sharedPreferences.getString("scan_tracker_location", "");
        }

        Set<String> logs = sharedPreferences.getStringSet("scan_log", new HashSet<>());
        if(logs != null){
            for(String log : logs){
                ScanLog scanLog = gson.fromJson(log, ScanLog.class);
                LinkedTreeMap<?, ?> json = (LinkedTreeMap<?, ?>) scanLog.getObject();
                if(json != null){
                    JsonObject content = gson.toJsonTree(json).getAsJsonObject();

                    Object object = json;
                    switch (scanLog.getType()){
                        case "inventory":
                            object = gson.fromJson(content, Inventory.class);
                            break;
                        case "container":
                            object = gson.fromJson(content, Container.class);
                            break;
                        case "item":
                            object = gson.fromJson(content, Item.class);
                            break;
                        default:
                            object = null;
                            break;
                    }
                    scanLog.setRecipient(object, scanLog.getType());
                }
                this.log.push(scanLog);
            }

            System.out.println("Loaded scan history (" + logs.size() + " items)");
            Collections.sort(this.log, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
        }

        Set<String> searchHistory = sharedPreferences.getStringSet("search_history", new HashSet<>());
        if(searchHistory != null){
            this.searchHistory.clear();
            for(String item : searchHistory){
                SearchHistoryItem searchHistoryItem = gson.fromJson(item, SearchHistoryItem.class);
                this.searchHistory.add(searchHistoryItem);
            }
            System.out.println("Loaded search history (" + this.searchHistory.size() + " entries)");
        }
    }
}
