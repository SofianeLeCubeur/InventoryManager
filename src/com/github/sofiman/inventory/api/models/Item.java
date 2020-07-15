package com.github.sofiman.inventory.api.models;

import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.api.Tag;

import java.util.List;

public interface Item extends Identifiable {

    String getName();
    String getInternalName();
    String getSerialNumber();
    String getDescription();
    String getState();
    LocationPoint getCurrentLocation();
    String getDetails();
    List<Tag> getTags();
    Container getParent();

    Item addTag(Tag tag);
    Item removeTag(Tag tag);
    Item setState(String state);
    Item pushLocation(LocationPoint location);
    Item setDetails(String details);
    List<LocationPoint> getLocationHistory();

}
