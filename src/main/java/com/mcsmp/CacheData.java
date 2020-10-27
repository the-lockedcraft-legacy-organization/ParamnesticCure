/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import com.mcsmp.database.DataBases;
import static com.mcsmp.ParamnesticCure.getInstance;
import java.util.HashMap;

/**
 * @author Frostalf
 */
public class CacheData {

    private HashMap<String, DataBases> databaseMap = new HashMap<>();
    private ParamnesticCure plugin = getInstance();
    private String address = plugin.getConfig().getString("address");
    private int port = plugin.getConfig().getInt("port");
    private String user = plugin.getConfig().getString("user");
    private String password = plugin.getConfig().getString("password");
    private String driver = plugin.getConfig().getString("driver");

    /**
     * Method to cache database connections.
     */
    public CacheData() {

        for (String databases : plugin.getConfig().getConfigurationSection("Database_Names").getKeys(false)) {
            if (!plugin.getConfig().getString("Database_Names." + databases + ".address").isEmpty()) {
                address = plugin.getConfig().getString("Database_Names." + databases + ".address");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".port").isEmpty()) {
                port = plugin.getConfig().getInt("Database_Names." + databases + ".port");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".user").isEmpty()) {
                user = plugin.getConfig().getString("Database_Names." + databases + ".user");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".password").isEmpty()) {
                password = plugin.getConfig().getString("Database_Names." + databases + ".password");
            }
            if (!plugin.getConfig().getString("Database_Names." + databases + ".driver").isEmpty()) {
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
    public HashMap<String, DataBases> getDatabaseMap() {
        return this.databaseMap;
    }

}
