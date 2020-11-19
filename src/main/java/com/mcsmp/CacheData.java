/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import com.mcsmp.database.DataBases;
import static java.lang.Byte.valueOf;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Frostalf
 */
public class CacheData {

    private ConcurrentHashMap<String, DataBases> databaseMap = new ConcurrentHashMap<>();
    private final ParamnesticCure plugin = ParamnesticCure.getInstance();
    private final byte givenVersion = valueOf(ParamnesticCure.getInstance().getConfig().getString("configVersion"));
    private String address = plugin.getConfig().getString("defaultconnection.address");
    private int port = plugin.getConfig().getInt("defaultconnection.port");
    private String user = plugin.getConfig().getString("user");
    private String password = plugin.getConfig().getString("defaultconnection.password");
    private String driver = plugin.getConfig().getString("defaultconnection.driver");

    /**
     * Method to cache database connections.
     */
    public CacheData() {

        for (String databases : plugin.getConfig().getConfigurationSection("Database_Names").getKeys(false)) {
            if (!plugin.getConfig().getString("Database_Names." + databases + ".address").isBlank()) {
                address = plugin.getConfig().getString("Database_Names." + databases + ".address");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".port").isBlank()) {
                port = plugin.getConfig().getInt("Database_Names." + databases + ".port");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".user").isBlank()) {
                user = plugin.getConfig().getString("Database_Names." + databases + ".user");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".password").isBlank()) {
                password = plugin.getConfig().getString("Database_Names." + databases + ".password");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".driver").isBlank()) {
                driver = plugin.getConfig().getString("Database_Names." + databases + ".driver");
            }
            DataBases bases = new DataBases(databases, plugin.getConfig().getString("Database_Names." + databases + ".database"), address, port, user, password, driver);
            databaseMap.put(databases.toLowerCase(), bases);
        }
    }
    /**
     * Method to return hashmap of databases.
     * @return Hashmap listing databases.
     */
    public ConcurrentHashMap<String, DataBases> getDatabaseMap() {
        return this.databaseMap;
    }

}
