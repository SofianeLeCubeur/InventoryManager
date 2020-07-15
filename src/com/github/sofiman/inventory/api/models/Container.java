package com.github.sofiman.inventory.api.models;

import com.github.sofiman.inventory.api.LocationPoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Container extends Identifiable {

    String getContentDescription();
    String getDetails();
    LocationPoint getCurrentLocation();
    String getState();
    Container getParent();

    Container pushLocation(LocationPoint location);
    Container setState(String state);
    Container setDetails(String details);

    /**
     * Register a new item
     * Warning: Don't forget to update this container to the database to apply changes
     * @param item Item to register to the container
     */
    void register(Item item);

    /**
     * Remove the item from the container
     * Warning: Don't forget to update this container to the database to apply changes;
     *          Caching is enabled and can be controlled by the database interface
     * @param id Item ID to remove
     * @return The removed item
     */
    Item pull(byte[] id);

    /**
     * Retrieve the item data from his ID
     * Warning: Caching is enabled and can be controlled by the database interface
     * @param id Item's ID
     * @return null if not found else the item
     */
    Item fetch(byte[] id);

    /**
     * @return all item instances registered, Warning: is the container was retrieved by the database, items may be not
     * up to date, use instead the getRegisteredItems method (updated when the container is got from the database)
     */
    Collection<Item> pullItems();

    /**
     * @return location history of the container
     */
    List<LocationPoint> getLocationHistory();

    /**
     * @return all registered items in this container, the map contains the date of registration and the item code
     */
    Map<Long, byte[]> getRegisteredItems();

}
