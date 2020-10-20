/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
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
