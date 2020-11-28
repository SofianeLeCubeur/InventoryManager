package com.github.sofiman.inventory.api;

public class Webhook {

    private String id;
    private String event;
    private String url;
    private WebhookDelivery last_delivery;

    public Webhook(String id, String event, String url, WebhookDelivery last_delivery) {
        this.id = id;
        this.event = event;
        this.url = url;
        this.last_delivery = last_delivery;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public WebhookDelivery getLastDelivery() {
        return last_delivery;
    }

    @Override
    public String toString() {
        return "Webhook{" +
                "id='" + id + '\'' +
                ", event='" + event + '\'' +
                ", url='" + url + '\'' +
                ", lastDelivery=" + last_delivery +
                '}';
    }

    public static class WebhookDelivery {

        public static final int STATUS_UNKNOWN = 0;
        public static final int STATUS_SUCCESS = 1;
        public static final int STATUS_ERROR = 2;

        private long timestamp;
        private int status;

        public WebhookDelivery(long timestamp, int status) {
            this.timestamp = timestamp;
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "WebhookDelivery{" +
                    "timestamp=" + timestamp +
                    ", status=" + status +
                    '}';
        }
    }
}
