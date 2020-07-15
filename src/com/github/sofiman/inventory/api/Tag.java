package com.github.sofiman.inventory.api;

import java.awt.Color;

public class Tag {

    private final String id;
    private final String label;
    private final Color color;

    public Tag(String id, String label, Color color) {
        this.id = id;
        this.label = label;
        this.color = color;
    }

    public String getId(){
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
