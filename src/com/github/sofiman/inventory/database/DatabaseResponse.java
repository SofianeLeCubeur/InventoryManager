package com.github.sofiman.inventory.database;

public interface DatabaseResponse<T> {

    void response(T callback);

    void error(DatabaseError error);
}