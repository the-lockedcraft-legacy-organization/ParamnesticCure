/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;



import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldInitEvent;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;

/**
 * @author Frostalf
 * @author Thorin
 */

/*
 * Paramnestic's listener class.
 */
public class ParamnesticCureListener implements Listener {

	
    private ConfigurationSection configSektion;

    /*
     * Constructor class
     * @param plugin Instance of the plugin.
     */
    public ParamnesticCureListener(ParamnesticCure plugin) {
        //
        configSektion = plugin.getConfig().getConfigurationSection("");
    }

    /**
     * Blockbreak actions are currently deemed as critical actions, some needs to get stored
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	
        //Tracked block checks if the block is creative/critical

		boolean isCreative = RestrictedCreativeAPI.isCreative(event.getBlock());

		Block block = event.getBlock();
		BlockState blockState = block.getState();
		
		
		ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
			@Override
			public void run() {
	    		TrackedBlocks.updateCreativeID(block,isCreative);
	    		
	    		
	    		/*
	    		 * Fixes the door scenario. Checks if the broken block is upper or lower.
	    		 * Then calls the update creative id function for the other part of the door
	    		 */
	    		if( blockState.getType().toString().contains("DOOR") ) {
	    			Location loc = blockState.getLocation();
	    			Bisected door = (Bisected) blockState.getBlockData();
	    			if(door.getHalf() == Bisected.Half.BOTTOM)
	    				loc = loc.add(0, 1, 0);
	    			else
	    				loc = loc.add(0,-1,0);
	    			
	    			TrackedBlocks.updateCreativeID(loc.getBlock(),isCreative);
	    		}
			}
    	},TrackedBlocks.waitPeriod);
       

    }
    /**
     * Currently not in use
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    	
    }
    /**
     * Checks for critical rollback commands
     * @param event Command being processed
     */
    @EventHandler
    public void commandRollBack(PlayerCommandPreprocessEvent event) {
    	String command = event.getMessage().toLowerCase();
    	
    	command = command.replaceAll(": ", ":");
    	String[] commandListed = command.split(" "); 
    	
    	
    	List<String> commandAlias = configSektion.getStringList("blockLoggerCommands.alias");
    	
    	int i = 0;
    	while(i < commandAlias.size()){
    		//the first string from commandListed will have / in front, all commandAlias should as well:
    		commandAlias.set(  i  ,  "/" + commandAlias.get(i)  );
    		i++;
    	}
    	
    	//if not containing alias or length is too low
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 2) return;
    	
    	if(loggerManager.createLoggerManager(commandListed, event.getPlayer().getLocation(),event.getPlayer()))
    		event.setCancelled(true);
    }
    /**
     * Checks for critical rollback commands
     * @param event Command being processed
     */
    @EventHandler
    public void serverCommandRollBack(ServerCommandEvent event) {
        //if someone types that alias cancel it.
    	String command = event.getCommand().toLowerCase();
    	command = command.replaceAll(": ", ":");
    	String[] commandListed = command.split(" ");
    	
    	List<String> commandAlias = configSektion.getStringList("blockLoggerCommands.alias");
    	
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 2) return;
    	
    	
    	if(loggerManager.createLoggerManager(commandListed, null, null));
    		event.setCancelled(true);
    }

    /**
     * 
     * @param event
     */
    @EventHandler
    public void worldInit(WorldInitEvent event) {
    	WorldManager.addWorldToDB(event.getWorld());
    }
}
