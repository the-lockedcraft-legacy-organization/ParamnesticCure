/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
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

    @Override
    public void onEnable() {
        //sets instance.
        instance = this;
        trackedBlocks = TrackedBlocks.getInstance();
        log = getLogger();
        //manages config
        this.saveDefaultConfig();
        byte givenVersion = (byte) getConfig().getInt("configVersion");
        final byte currentVersion = 4;
        if (givenVersion == currentVersion) {
            log.info("[Debug] Your config is up to date!.");
        } else {
            log.info("[Debug] Your config was outdated. It has been updated to v{0}" + currentVersion);
            //TODO add config updater.
            getConfig().set("configVersion", currentVersion);
        }
        log.warning("[Dev] You have enabled an early development version of this plugin.");
        log.warning("[Dev] It will probably be unstable");
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

}
