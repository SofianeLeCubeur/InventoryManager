package com.github.sofiman.inventory.api.models;

import com.github.sofiman.inventory.api.Trigger;

public interface Tracker extends Identifiable {

    String getLabel();
    byte[] getTargetId();
    int getTargetType();
    Trigger getTrigger();
}
