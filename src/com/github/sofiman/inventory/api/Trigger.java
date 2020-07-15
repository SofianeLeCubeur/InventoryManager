package com.github.sofiman.inventory.api;

public enum Trigger {
    /**
     * When the watched object was scanned with database-connected device
     */
    SCAN,
    /**
     * When the watched object is being updated in the database
     */
    UPDATE,
    /**
     * When the watched object is being deleted from the database
     */
    REMOVE,
    /**
     * When a parent is assigned to the watched object
     */
    PARENT_ASSIGNMENT
}
