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
        
        Block block = event.getBlock();

        BlockState blockState = block.getState();

        plugin.getLogger().info("[Manual Debug] You broke a block hasMetadata('GMC')= " + RestrictedCreativeAPI.isCreative(event.getBlock()));
        
        //check if creative
        if(RestrictedCreativeAPI.isCreative(event.getBlock())) {
        	//TODO logic that compares this to the CO database
            plugin.getTrackedBlocks().updateCreativeIDInDB(block.getLocation());
        }

    }
    /**
     * Checks removes Φ status of any manually placed blocks
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //block is block <= yes that seems about right
    	if(!RestrictedCreativeAPI.isCreative(event.getBlock())) return;
        plugin.getLogger().info("[Manual Debug] You placed a block in creative");
    }
    /**
     * Detects if someone is attempting a rollback operation... and runs a tree of logic to ensure it doesn't mess with creative data.
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
    		RollbackManager rollback = new RollbackManager(  Arrays.copyOfRange(commandListed, 2, commandListed.length),   event.getPlayer().getLocation()   );
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
     * If someone tries a rollback command from console, tell them not to.
     * Console-based rollbacks add another layer of complexity that we would prefer to avoid at present.
     * @param event Command being processed
     */
    @EventHandler
    public void serverCommandRollBack(ServerCommandEvent event) {
        //if someone types that alias cancel it.
    	String command = event.getCommand().toLowerCase();
    	String[] commandListed = command.split(" ");
    	
    	if(!configSektion.getStringList("blockLoggerCommands.alias").contains(commandListed[0])) {
            return;
    	}
    	log.warning("Console rollbacks are not yet supported by Paramnestic.");
        event.setCancelled(true);
    }
}
