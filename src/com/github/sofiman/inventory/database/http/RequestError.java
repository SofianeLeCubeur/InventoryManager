package com.github.sofiman.inventory.database.http;

public class RequestError {

    private final int statusCode;
    private final String err;
    private final String err_description;

    public RequestError(int statusCode, String err, String err_description) {
        this.statusCode = statusCode;
        this.err = err;
        this.err_description = err_description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return err;
    }

    public String getDescription() {
        return err_description;
    }

    @Override
    public String toString() {
        return err + " (" + statusCode + "): " + err_description;
    }
}
