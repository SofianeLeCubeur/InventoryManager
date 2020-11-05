package com.github.sofiman.inventory.api;

import java.net.URI;

public class Server {

    private String name;
    private String endpoint;
    private boolean defaultServer;

    public Server(String name, String url) {
        this.name = name;
        this.endpoint = url;
    }

    public Server setAsDefaultServer(boolean defaultServer) {
        this.defaultServer = defaultServer;
        return this;
    }

    public boolean isDefaultServer() {
        return defaultServer;
    }

    public String getName() {
        return name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "Server{" +
                "name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
