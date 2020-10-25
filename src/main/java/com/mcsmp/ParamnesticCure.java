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
//Plugin's main class.
public class ParamnesticCure extends JavaPlugin {

    //Initializes the main class, TrackedBlocks, and implements the Logger.
    private static ParamnesticCure instance;
    private TrackedBlocks trackedBlocks;
    private Logger log = Bukkit.getLogger();
    private CacheData dataCache;
    private boolean everythingOK = true;

    //Import Config
    //private static final String driver = ParamnesticCure.getConfig().getString("databases.driver");


    @Override
    public void onEnable() {
        dataCache = new CacheData();
        //sets instance.
        instance = this;
        trackedBlocks = TrackedBlocks.getInstance();
        log = getLogger();

        //manages config
        //checks version of config
        final byte givenVersion = valueOf(getConfig().getString("configVersion"));
        final String driver = getConfig().getString("databases.driver");
        final byte currentVersion = 6;
        File configVar = new File(getDataFolder(), "config.yml");
        //if outdated config, rename old config and install new config.
        if (givenVersion < currentVersion) {
            if (configVar.exists()){
                configVar.renameTo(new File(getDataFolder(), "config.old"));
            }
            this.saveDefaultConfig();
            log.warning("[Debug] Invalid config! This is either your first time running PC, or, the config has updated since you last used it.");
            log.severe("[Debug] Providing you with a new config; please fill it out before running PC.");
            getPluginManager().disablePlugin(this);
        //Needs a database atm
        //Loads the config
        } else {
            log.info("[Debug] Loaded Config");
            log.warning("[Dev] You have enabled an early development version of this plugin.");
            log.warning("[Dev] It will probably be unstable");
        }
    }

    //Method to get PC instance
    public static ParamnesticCure getInstance() {
        return instance;
    }

    //Method to get tracked blocks (and initialize instance of it).
    public TrackedBlocks getTrackedBlocks() {
        if (trackedBlocks == null) {
            trackedBlocks = TrackedBlocks.getInstance();
        }
        return trackedBlocks;
    }

    public CacheData getCacheData() {
        return this.dataCache;
    }

    public void setOK(boolean ok) {
        this.everythingOK = ok;
    }
}
