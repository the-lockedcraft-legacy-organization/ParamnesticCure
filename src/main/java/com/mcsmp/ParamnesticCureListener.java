/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
    	
        //Tracked block checks if the block is creative/critical
        TrackedBlocks.updateCreativeID(event.getBlock());

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
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 3) return;
    	
    	loggerManager.createLoggerManager(commandListed, event.getPlayer().getLocation());
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
    	
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 3) return;
    	
    	
    	loggerManager.createLoggerManager(commandListed, null);
    }
}
