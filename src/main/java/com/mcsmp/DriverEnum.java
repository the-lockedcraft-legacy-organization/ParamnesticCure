/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

/**
 * @author Frostalf
 */

/**
 * Enum to list drivers.
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
