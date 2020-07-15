package com.github.sofiman.inventory.api.models;

public interface ScanLog {

    /**
     * @return The device which did the scan
     */
    String getDevice();
    /**
     * @return The location of the scanner
     */
    String getLocation();
    /**
     * @return The date of the scan
     */
    long getTimestamp();

    /**
     * @return the id of the object contained in the scanned code, returns null if no match was found
     */
    String getRecipient();
    /**
     * @return the date of wich the scanned code was created
     */
    long getRecipientDate();
}
