/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.io.File;
import static java.lang.Byte.valueOf;
import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;

import static org.bukkit.Bukkit.getPluginManager;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
/**
 * @author Frostalf
 * @author Thorin
 */

/*
 * Plugin's main class.
 */
public class ParamnesticCure extends JavaPlugin {

    private static ParamnesticCure instance;
    private Logger log = Bukkit.getLogger();
    private CacheData dataCache;
    private Connection connection;
    private boolean isMySql;

    
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new ParamnesticCureListener(this), this);
        log = getLogger();
        
        final byte givenVersion = valueOf(getConfig().getString("configVersion"));
        //Temporary variable indicating desired config version.
        //Should ideally be maven-based, but currently isn't due to a bug.
        final byte currentVersion = 8;
        File configVar = new File(getDataFolder(), "config.yml");
        //if outdated config, rename old config and install a new one.

        // Manages plugins config.
        if (givenVersion != currentVersion) {
            if (configVar.exists()){
                configVar.renameTo(new File(getDataFolder(), "config.old"));
            }
            this.saveDefaultConfig();
            log.warning("[Startup] Invalid config! This is either your first time running PC, or, the config has updated since you last used it.");
            log.severe("[Startup] Providing you with a new config; please fill it out before running PC.");
            getPluginManager().disablePlugin(this);
        } else {
            log.info("[Startup] Loaded Config");
            //Temporary Warning.
            log.warning("[Dev] You have enabled an early development version of this plugin.");
            log.warning("[Dev] It will probably be unstable");
        }
        
        //Creates new cache
        final String driver = getConfig().getString("defaultconnection.driver");
        isMySql = (driver.toLowerCase() != "sqlite");
        log.info("[Debug] Set driver to " + driver);
        //sets instance.
        instance = this;
        dataCache = new CacheData();
        
        try {
        	this.connection = getCacheData().getDatabaseMap().get("paramnestic").getDatabase().getConnection();
        }
        catch(SQLException ex) 
        {
        	ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());
            getPluginManager().disablePlugin(this);
        }

        createDB();
    }

    @Override
    public void onDisable() {
    	try {
        connection.close();
    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    }

    /**
     * Gets this plugin's instance.
     * @return Returns plugin's instance.
     */
    public static ParamnesticCure getInstance() {
        return instance;
    }
    /**
     * Gets cached data
     * @return Returns dataCache
     */
    public CacheData getCacheData() {
        return this.dataCache;
    }
    
    
    /**
     * Creates all the databases for this plugin, also adds all the worlds to the worlds database (if not done already)
     */
    
    private void createDB() {
    	try {
    		Connection connection = getConnection();
    		PreparedStatement statement = connection.prepareStatement(
    				"CREATE TABLE IF NOT EXISTS blockAction"
    				+ " (time INTEGER,world INTEGER,x INTEGER, y INTEGER, z INTEGER, is_creative INTEGER"
    				+ " ,UNIQUE(time,world,x,y,z));"
    				);
    		statement.execute();
    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	
    	try {
    		Connection connection = getConnection();
    		PreparedStatement statement = connection.prepareStatement(
    				"CREATE TABLE IF NOT EXISTS worlds ("
    				+ "world_id INTEGER PRIMARY KEY "+ (isMySql ? "AUTO_INCREMENT":"AUTOINCREMENT")
    				+ ", world VARCHAR(255), UNIQUE(world) );"
    				);
    		statement.execute();
    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	
    	List<World> worldlist = ParamnesticCure.getInstance().getServer().getWorlds();
    	
    	for(World world : worldlist) {
    		WorldManager.addWorldToDB(world);
    	}
    }
    /**
     * 
     * @return returns a CoreProtectAPI instance, otherwise if any problems arise returns null
     */
    public CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");
     
        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 6) {
            return null;
        }

        return CoreProtect;
    }
    /**
     * Whether this is a good solution or not, i don't know. But it gives no errors
     * @return Return's this plugin's shared connection
     */
    public Connection getConnection() {
    	return connection;
    }
}
