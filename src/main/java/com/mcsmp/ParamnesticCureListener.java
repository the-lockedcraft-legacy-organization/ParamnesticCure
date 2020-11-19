/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import static com.mcsmp.ParamnesticCure.getInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * @author Frostalf
 */

/*
 * Paramnestic's listener class.
 */
public class ParamnesticCureListener implements Listener {

    //Establishes plugin's instance
    private ParamnesticCure plugin;
    //Establishes logger
    private Logger log = getLogger();
    //Makes an empty list for the logger's rollback aliases.
    private List<String> rbAlias;

    /*
     * Constructor class
     * @param plugin Instance of the plugin.
     */
    public ParamnesticCureListener(ParamnesticCure plugin) {
        this.plugin = plugin;
        rbAlias = new ArrayList<>();
        //Populates rbAlias with all the aliases specified in the config.
        rbAlias.addAll(plugin.getConfig().getConfigurationSection("").getStringList("blockLoggerRollbackCommands"));
    }

    /**
     * Checks ζ block status of manually broken blocks (currently using NBT) sets them as Φ
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        //block is the Block broken
        Block block = event.getBlock();
        //blockState is the BlockState of that block
        BlockState blockState = block.getState();
        //adds any block w/ meta GMC to PC's block list.
        if(blockState.hasMetadata("GMC")) {
            plugin.getTrackedBlocks().addToBlockList(block.getLocation());
        }

    }
    /**
     * Checks removes Φ status of any manually placed blocks
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //block is block
        Block block = event.getBlock();
        //if block is tracked, untrack it.
        if(plugin.getTrackedBlocks().isTracked(block.getLocation())) {
            plugin.getTrackedBlocks().removeFromBlockList(block.getLocation());
        }
    }
    /**
     * Detects if someone is attempting a rollback operation... and runs a tree of logic to ensure it doesn't mess with creative data.
     * @param event Command being processed
     */
    @EventHandler
    public void commandRollBack(PlayerCommandPreprocessEvent event) {
        //If the command starts with a slash, and would result in a rollback, consider it a rollback operation.
        String slash = "/";
        for (String command : rbAlias) {
            if(event.getMessage().equalsIgnoreCase(slash.concat(command)));
                //Start running paramnestic's rollback logic.
                RollbackManager rollback = new RollbackManager();
                rollback.executeTask();
            }
        }

    /**
     * If someone tries a rollback command from console, tell them not to.
     * Console-based rollbacks add another layer of complexity that we would prefer to avoid at present.
     * @param event Command being processed
     */
    @EventHandler
    public void serverCommandRollBack(ServerCommandEvent event) {
        //if someone types that alias cancel it.
        for (String command : rbAlias) {
            if(event.getCommand().equalsIgnoreCase(command)){
                log.warning("Console rollbacks are not yet supported by Paramnestic.");
                event.setCancelled(true);
            }
        }
    }
}
