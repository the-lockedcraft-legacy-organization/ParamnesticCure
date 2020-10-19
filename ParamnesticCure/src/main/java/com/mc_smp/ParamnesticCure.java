/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mc_smp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Frostalf
 */
public class ParamnesticCure extends JavaPlugin {

    private static ParamnesticCure instance;
    private TrackedBlocks trackedBlocks;
    private Logger log;

    @Override
    public void onEnable() {
        instance = this;
        trackedBlocks = TrackedBlocks.getInstance();
        log = getLogger();
        this.saveDefaultConfig();
        byte givenVersion = (byte) getConfig().getInt("configVersion");
        byte currentVersion = 1;
        if (givenVersion == currentVersion) {
            log.info("[Paramnestic] [Debug] Your config is up to date!.");
        } else {
            log.log(Level.INFO, "[Paramnestic] [Debug] Your config was outdated. It has been updated to v{0}", currentVersion);
            getConfig().set("configVersion", currentVersion);
        }
        log.warning("[Paramnestic] [Dev] You have enabled an early development version of this plugin.");
        log.warning("[Paramnestic] [Dev] It will probably be unstable");
    }

    public static ParamnesticCure getInstance() {
        return instance;
    }

    public TrackedBlocks getTrackedBlocks() {
        if (trackedBlocks == null) {
            trackedBlocks = TrackedBlocks.getInstance();
        }
        return trackedBlocks;
    }

}
