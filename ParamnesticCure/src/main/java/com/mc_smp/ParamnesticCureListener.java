/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
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
//This is a listener class.
public class ParamnesticCureListener implements Listener {

    //The object plugin is of the main class
    private ParamnesticCure plugin;
    //Establishes logger
    private Logger log = Bukkit.getLogger();
    //Makes an empty list for the rollback aliases.
    private List<String> rbAlias = new ArrayList<>(ParamnesticCure.getInstance().getConfig().getList("blockLoggerRollbackCommands").size());

    //Sets plugin instance
    public ParamnesticCureListener(ParamnesticCure plugin) {
        this.plugin = plugin;
        //Populates rbAlias with all the aliases specified in the config.
        rbAlias.addAll(plugin.getConfig().getConfigurationSection("").getStringList("blockLoggerRollbackCommands"));   
    }
    
    //on the block break event,
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        //block is the Block broken
        Block block = event.getBlock();
        //blockState is the BlockState of that block
        BlockState blockState = block.getState();
        //adds any block w/ meta GMC  to PC's block list.
        if(blockState.hasMetadata("GMC")) {
            plugin.getTrackedBlocks().addToBlockList(block);
        }

    }
    //on the block place event,
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //block is block
        Block block = event.getBlock();
        //if block is tracked, untrack it.
        if(plugin.getTrackedBlocks().isTracked(block)) {
            plugin.getTrackedBlocks().removeFromBlockList(block);
        }
    }
    //if someone types a command pre-proccess
    @EventHandler
    public void commandRollBack(PlayerCommandPreprocessEvent event) {
        String slash = "/";
        //if someone types that alias and starts it with a slash, then do stuff.
        for (String command : rbAlias) {
            if(event.getMessage().equalsIgnoreCase(slash.concat(command)));
                //TODO implement task logic r/ db.
            }
        }

    @EventHandler
    //If the server receives a command
    public void serverCommandRollBack(ServerCommandEvent event) {
        //if someone types that alias cancel it.
        for (String command : rbAlias) {
            if(event.getCommand().equalsIgnoreCase(command)){
                log.info("Console rollbacks are not yet supported by Paramnestic.");
                event.setCancelled(true);                 
            }
        }
    }
}
