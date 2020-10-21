package com.github.sofiman.inventory.api;

public class Token {

    private String token;
    private String type;

    public Token(String token, String type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return type;
    }

    public String asAuthorization(){
        return type + " " + token;
    }
}
