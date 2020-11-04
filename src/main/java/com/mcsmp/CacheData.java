/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import com.mcsmp.database.DataBases;
import static java.lang.Byte.valueOf;
import java.util.HashMap;

/**
 * @author Frostalf
 */
public class CacheData {

    private HashMap<String, DataBases> databaseMap = new HashMap<>();
    private final ParamnesticCure plugin = ParamnesticCure.getInstance();
    final byte givenVersion = valueOf(ParamnesticCure.getInstance().getConfig().getString("configVersion"));
    private String address = ParamnesticCure.getInstance().getConfig().getString("defaultconnection.address");
    private int port = ParamnesticCure.getInstance().getConfig().getInt("defaultconnection.port");
    private String user = ParamnesticCure.getInstance().getConfig().getString("user");
    private String password = ParamnesticCure.getInstance().getConfig().getString("defaultconnection.password");
    private String driver = ParamnesticCure.getInstance().getConfig().getString("defaultconnection.driver");

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
    public HashMap<String, DataBases> getDatabaseMap() {
        return this.databaseMap;
    }

}
