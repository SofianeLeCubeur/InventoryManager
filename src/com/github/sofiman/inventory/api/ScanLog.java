package com.github.sofiman.inventory.api;

public class ScanLog {

    private String content;
    private String device;
    private String location;
    private long timestamp;
    private Object object;
    private String type;

    public ScanLog(String device, String location, String content, long timestamp) {
        this.device = device;
        this.location = location;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ScanLog setRecipient(Object obj, String type){
        this.object = obj;
        this.type = type;

        return this;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object getObject() {
        return object;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ScanLog{" +
                "content='" + content + '\'' +
                ", device='" + device + '\'' +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                ", objectClass=" + object.getClass().getName() +
                ", object=" + object +
                ", type='" + type + '\'' +
                '}';
    }
}
