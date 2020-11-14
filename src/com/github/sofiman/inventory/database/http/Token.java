package com.github.sofiman.inventory.database.http;

public class Token {

    private final String token;
    private final String type;

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
