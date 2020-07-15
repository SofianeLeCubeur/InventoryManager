package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.ScanLog;

public class ScanLogImpl implements ScanLog {

    private final String device;
    private final String location;
    private final long timestamp;
    private final String recipientId;
    private final long recipientDate;

    public ScanLogImpl(String device, String location, long timestamp, String recipientId, long recipientDate) {
        this.device = device;
        this.location = location;
        this.timestamp = timestamp;
        this.recipientId = recipientId;
        this.recipientDate = recipientDate;
    }

    @Override
    public String getDevice() {
        return device;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getRecipient() {
        return recipientId;
    }

    @Override
    public long getRecipientDate() {
        return recipientDate;
    }

    @Override
    public String toString() {
        return "ScanLog{" +
                "device='" + device + '\'' +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                ", recipientId='" + recipientId + '\'' +
                ", recipientDate=" + recipientDate +
                '}';
    }
}
