/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import static com.mcsmp.DriverEnum.SQLITE;
import java.io.File;
import static java.lang.Byte.valueOf;
import java.util.logging.Logger;
import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getPluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Frostalf
 */

/*
 * Plugin's main class.
 */
public class ParamnesticCure extends JavaPlugin {

    //Initializes the main class, TrackedBlocks, and implements the Logger.
    private static ParamnesticCure instance;
    private TrackedBlocks trackedBlocks;
    private Logger log = Bukkit.getLogger();
    private CacheData dataCache;
    //Variable to ensure databases are connected properly.
    private boolean everythingOK = true;


    /*
     * Codeblock that executes when plugin is enabled.
     */
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        log = getLogger();
        log.warning("[Debug] Perhaps that created the config?");
        final byte givenVersion = valueOf(getConfig().getString("configVersion"));
        //Temporary variable indicating desired config version.
        //Should ideally be maven-based, but currently isn't due to a bug.
        final byte currentVersion = 7;
        File configVar = new File(getDataFolder(), "config.yml");
        log.warning("[Debug] Created internal config.yml reference.");
        //if outdated config, rename old config and install a new one.
        
        // Manages plugins config.
        if (givenVersion != currentVersion) {
            if (configVar.exists()){
                configVar.renameTo(new File(getDataFolder(), "config.old"));
            }
            this.saveDefaultConfig();
            log.warning("[Startup] Invalid config! This is either your first time running PC, or, the config has updated since you last used it.");
            log.severe("[Startup] Providing you with a new config; please fill it out before running PC.");
            setOK(false);
            getPluginManager().disablePlugin(this);
        } else {
            log.info("[Startup] Loaded Config");
            //Temporary Warning.
            log.warning("[Dev] You have enabled an early development version of this plugin.");
            log.warning("[Dev] It will probably be unstable");
        }
        //Creates new cache
        final String driver = getConfig().getString("databases.driver");
        log.warning("[Debug] That appears to work. Driver was set to" + driver);
        dataCache = new CacheData();
        //sets instance.
        instance = this;
        trackedBlocks = TrackedBlocks.getInstance();
    }

    /*
     * Gets this plugin's instance.
     * @return Returns plugin's instance.
     */
    public static ParamnesticCure getInstance() {
        return instance;
    }

    /*
     * Initializes an instance of TrackedBlocks
     * @return Returns TrackedBlocks
     */
    public TrackedBlocks getTrackedBlocks() {
        if (trackedBlocks == null) {
            trackedBlocks = TrackedBlocks.getInstance();
        }
        return trackedBlocks;
    }

    /*
     * Gets cached data
     * @return Returns dataCache
     */
    public CacheData getCacheData() {
        return this.dataCache;
    }

    /*
     * Sets default value of db status.
     * @return if startup can proceed.
     */
    public void setOK(boolean ok) {
        this.everythingOK = ok;
    }
}
