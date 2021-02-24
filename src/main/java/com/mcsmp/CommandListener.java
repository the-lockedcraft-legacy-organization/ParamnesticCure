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
public class CommandListener implements Listener {

	
    private ConfigurationSection configSektion;

    /*
     * Constructor class
     * @param plugin Instance of the plugin.
     */
    public CommandListener(ParamnesticCure plugin) {
        //
        configSektion = plugin.getConfig().getConfigurationSection("");
    }
    /**
     * Checks for commands that should be intercepted
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
    	
    	//Unnecessary to process already faulty commands
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 2) return;
    	
    	if(LoggerManager.createLoggerManager(commandListed, event.getPlayer().getLocation(),event.getPlayer()))
    		event.setCancelled(true);
    }
    /**
     * Checks for commands that should be intercepted
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
    	
    	
    	if(LoggerManager.createLoggerManager(commandListed, null, null));
    		event.setCancelled(true);
    }

}
