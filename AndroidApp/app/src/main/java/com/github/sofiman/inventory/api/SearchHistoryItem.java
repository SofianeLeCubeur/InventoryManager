package com.github.sofiman.inventory.api;

public class SearchHistoryItem {

    private String query;
    private long timestamp;
    private String type;
    private int resultCount;

    public SearchHistoryItem(String query, long timestamp, String type, int resultCount) {
        this.query = query;
        this.timestamp = timestamp;
        this.type = type;
        this.resultCount = resultCount;
    }

    public String getQuery() {
        return query;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public int getResultCount() {
        return resultCount;
    }
}
