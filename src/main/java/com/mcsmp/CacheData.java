/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import com.mcsmp.database.DataBases;
import static com.mcsmp.ParamnesticCure.getInstance;
import static java.lang.Byte.valueOf;
import java.util.HashMap;
import org.bukkit.Bukkit;

/**
 * @author Frostalf
 */
public class CacheData {

    private HashMap<String, DataBases> databaseMap = new HashMap<>();
    private ParamnesticCure plugin;
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

        for (String databases : ParamnesticCure.getInstance().getConfig().getConfigurationSection("Database_Names").getKeys(false)) {
            if (!ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".address").isEmpty()) {
                address = ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".address");
            }
            if (!ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".port").isEmpty()) {
                port = ParamnesticCure.getInstance().getConfig().getInt("Database_Names." + databases + ".port");
            }
            if (!ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".user").isEmpty()) {
                user = ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".user");
            }
            if (!ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".password").isEmpty()) {
                password = ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".password");
            }
            if (!ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".driver").isEmpty()) {
                driver = ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".driver");
            }
            DataBases bases = new DataBases(databases, ParamnesticCure.getInstance().getConfig().getString("Database_Names." + databases + ".database"), address, port, user, password, driver);
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
