package com.mcsmp;

import com.mcsmp.database.DataBases;
import java.util.HashMap;

/**
 *
 * @author Frostalf
 */
public class CacheData {

    private HashMap<String, DataBases> databaseMap = new HashMap<>();
    private ParamnesticCure plugin = ParamnesticCure.getInstance();
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

    public HashMap<String, DataBases> getDatabaseMap() {
        return this.databaseMap;
    }

}
