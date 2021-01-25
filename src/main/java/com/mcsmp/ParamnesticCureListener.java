/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
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
    private ParamnesticCure plugin = ParamnesticCure.getInstance();
    //Establishes logger
    private Logger log = getLogger();
    //Makes an empty list for the logger's rollback aliases.
    private ConfigurationSection configSektion;

    /*
     * Constructor class
     * @param plugin Instance of the plugin.
     */
    public ParamnesticCureListener(ParamnesticCure plugin) {
        this.plugin = plugin;
        //
        configSektion = plugin.getConfig().getConfigurationSection("");
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
        plugin.getLogger().info("[Manual Debug] You broke a block hasMetadata('GMC')= " + blockState.hasMetadata("GMC"));
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
        plugin.getLogger().info("[Manual Debug] You placed a block: hasMetadata('GMC')= " + block.getState().hasMetadata("GMC"));
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
    	
    	String command = event.getMessage().toLowerCase();
    	String[] commandListed = command.split(" "); //All hail the father of List
    	
    	List<String> commandIdentifiers = configSektion.getStringList("blockLoggerCommands.alias");
    	
    	//to lazy to find a command for this
    	int i = 0;
    	while(i++ < commandIdentifiers.size()){
    		//the first string from commandListed will have / in front, all commandIdentifiers should as well:
    		commandIdentifiers.set(i, "/" + commandIdentifiers.get(i));
    	}
    	
    	//if not containing alias
    	if(!commandIdentifiers.contains(commandListed[0])) return;
    	
    	if(configSektion.getStringList("blockLoggerCommands.rollback").contains(commandListed[1])) { 
    		plugin.getLogger().info("[Manual Debug] Triggered as a rollback");
    		//innitiate rollbackmanager
    		RollbackManager rollback = new RollbackManager(   Arrays.copyOfRange(commandListed, 2, commandListed.length),   event.getPlayer().getLocation()   );
    		rollback.executeTask();
    	}
    	if(configSektion.getStringList("blockLoggerCommands.restore").contains(commandListed[1])) {
    		 //innitiate restoremanager (not created yet)
    		plugin.getLogger().info("[Manual Debug] Triggered as a restore");
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
    	String command = event.getCommand().toLowerCase();
    	String[] commandListed = command.split(" ");
    	
    	if(configSektion.getStringList("blockLoggerCommands.alias").contains(commandListed[0])) {
            log.warning("Console rollbacks are not yet supported by Paramnestic.");
            event.setCancelled(true);
    	}
    }
}
