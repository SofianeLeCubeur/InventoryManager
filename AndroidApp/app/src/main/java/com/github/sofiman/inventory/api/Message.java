package com.github.sofiman.inventory.api;

public class Message {

    private String initiator;
    private String message;

    public Message(String initiator, String message) {
        this.initiator = initiator;
        this.message = message;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getMessage() {
        return message;
    }
}
