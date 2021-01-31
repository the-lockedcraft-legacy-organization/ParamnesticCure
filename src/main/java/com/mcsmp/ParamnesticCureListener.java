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

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;

/**
 * @author Frostalf
 * @author Thorin
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
     * Blockbreak actions are currently deemed as critical actions, some needs to get stored
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        plugin.getLogger().info("[Manual Debug] You broke a block hasMetadata('GMC')= " + RestrictedCreativeAPI.isCreative(event.getBlock()));
        
        //Tracked block checks if the block is creative/critical
        TrackedBlocks.updateCreativeIDInDB(event.getBlock());

    }
    /**
     * Currently not in use
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //block is block <= yes that seems about right
    	if(!RestrictedCreativeAPI.isCreative(event.getBlock())) return;
        plugin.getLogger().info("[Manual Debug] You placed a block in creative");
    }
    /**
     * Checks for critical rollback commands
     * @param event Command being processed
     */
    @EventHandler
    public void commandRollBack(PlayerCommandPreprocessEvent event) {
    	String command = event.getMessage().toLowerCase();
    	String[] commandListed = command.split(" "); //All hail the father of List
    	
    	List<String> commandAlias = configSektion.getStringList("blockLoggerCommands.alias");
    	
    	int i = 0;
    	while(i < commandAlias.size()){
    		//the first string from commandListed will have / in front, all commandAlias should as well:
    		commandAlias.set(  i  ,  "/" + commandAlias.get(i)  );
    		i++;
    	}
    	
    	//if not containing alias or length is too low
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 3) return;
    	
    	
    	List<String> rollbackAlias = configSektion.getStringList("blockLoggerCommands.rollback");
    	if(rollbackAlias.contains(commandListed[1])) { 
    		plugin.getLogger().info("[Manual Debug] Triggered as a rollback");
    		//innitiate rollbackmanager
    		RollbackManager rollback = new RollbackManager(  Arrays.copyOfRange(commandListed, 2, commandListed.length),   event.getPlayer().getLocation()  );
    		rollback.executeTask();
    		event.setCancelled(true);
    	}
    	List<String> restoreAlias = configSektion.getStringList("blockLoggerCommands.restore");
    	if(restoreAlias.contains(commandListed[1])) {
    		 //innitiate restoremanager (not created yet)
    		plugin.getLogger().info("[Manual Debug] Triggered as a restore");
    	}
    }
    /**
     * Console based commands currently not implemented
     * @param event Command being processed
     */
    @EventHandler
    public void serverCommandRollBack(ServerCommandEvent event) {
        //if someone types that alias cancel it.
    	String command = event.getCommand().toLowerCase();
    	String[] commandListed = command.split(" ");
    	
    	if(!configSektion.getStringList("blockLoggerCommands.alias").contains(commandListed[0])||commandListed.length<3) return;
    	
    	log.warning("Console rollbacks are not yet supported by Paramnestic.");
        event.setCancelled(true);
    }
}
