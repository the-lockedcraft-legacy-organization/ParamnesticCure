package com.mcsmp;

/**
 *
 * @author Frostalf
 */


public enum DriverEnum {

    MYSQL("mysql"),
    SQLITE("sqlite"),
    POSTREGSQL("postregsql");

    String driver;
    DriverEnum(String driver) {
        this.driver = driver.toUpperCase();
    }
}
