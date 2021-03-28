/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.commands;



import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.mcsmp.MessageManager;
import com.mcsmp.ParamnesticCure;
import com.mcsmp.block.BlockListener;
import com.mcsmp.block.RestoreManager;
import com.mcsmp.block.RollbackManager;

/**
 * Tracks commands, and checks whether they should be intercepted
 * @author Frostalf
 * @author Thorin
 */
public class CommandListener implements Listener {


	private static HashMap<String,String[]> storedCommands = new HashMap<String,String[]>();
	
    private ConfigurationSection configSektion;

    /*
     * Constructor class
     * @param plugin Instance of the plugin.
     */
    public CommandListener(ParamnesticCure plugin) {
        //
        configSektion = plugin.getConfig().getConfigurationSection("");
    }
    
    
    //EVENT HANDLERS
    
    /**
     * Checks for commands that should be intercepted
     * @param event Command being processed
     */
    @EventHandler
    public void playerCommand(PlayerCommandPreprocessEvent event) {
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
    	
    	if(createLoggerManager(commandListed, event.getPlayer().getLocation(),event.getPlayer()))
    		event.setCancelled(true);
    	else
    		ParamnesticCure.getInstance().getLogger().fine("[CommandTracker.playerComand] intercept cancelled");
    }
    /**
     * Checks for commands that should be intercepted
     * @param event Command being processed
     */
    @EventHandler
    public void serverCommand(ServerCommandEvent event) {
        //if someone types that alias cancel it.
    	String command = event.getCommand().toLowerCase();
    	command = command.replaceAll(": ", ":");
    	String[] commandListed = command.split(" ");
    	
    	List<String> commandAlias = configSektion.getStringList("blockLoggerCommands.alias");
    	
    	if(!commandAlias.contains(commandListed[0])||commandListed.length < 2) return;
    	
    	
    	if(createLoggerManager(commandListed, null, null));
    		event.setCancelled(true);
    }

	
	
	//INTERNAL FUNCTIONS
	
    
    /**
	 * Interprets a part of the command to see if a rollback,restore,undo, or purge should be created
	 * also does some relevant setup for those commands
	 * @param command The arguments after the logger alias
	 * @param location Location of the player (can be null)
	 * @param playerOperator the player who initiated the command
	 * @return true if command event should be cancelled
	 */
	private boolean createLoggerManager(String[] command, Location location, Player playerOperator) {
		
		String operator = (playerOperator == null) ? null : playerOperator.getName();
		
		
		List<String> rollbackAlias = configSektion.getStringList("blockLoggerCommands.rollback");
		String[] arguments = Arrays.copyOfRange(command, 2, command.length);
		
    	if(rollbackAlias.contains(command[1])) { 
    		if(!PermissionManager.hasRollback(playerOperator)) return false;
    		
    		RollbackManager rollback = new RollbackManager( arguments , location , playerOperator);
    		if(!rollback.executeTask()) {
    			ParamnesticCure.getInstance().getLogger().finest("[CommandTracker.createLoggerManager] this function is going to return false");
    			return false;
    		}
    		storeCommand(operator,command);
    		return true;
    	}
    	List<String> restoreAlias = configSektion.getStringList("blockLoggerCommands.restore");
    	if(restoreAlias.contains(command[1])) {
    		
    		if(!PermissionManager.hasRestore(playerOperator)) return false;
    		
    		
    		RestoreManager restore = new RestoreManager(  arguments, location , playerOperator );
    		if(!restore.executeTask())
    			return false;
    		storeCommand(operator,command);
    		return true;
    	}
    	List<String> undoAlias = configSektion.getStringList("blockLoggerCommands.undo");
    	if(undoAlias.contains(command[1])) {
    		if(!(PermissionManager.hasRollback(playerOperator)&&PermissionManager.hasRestore(playerOperator))) return false;
    		//TODO make better undo's, that store location and takes time into consideration
    		
    		String player = operator;
    		
    		if(command.length == 3)
    			player = command[2];
    		else if(command.length > 3) {
    			return false;
    		}
    		if (undoCommand(player, operator, location))
    			return true;
    	}
    	List<String> purgeAlias = configSektion.getStringList("blockLoggerCommands.purge");
    	if(purgeAlias.contains(command[1])) {
    		if(!PermissionManager.hasPurge(playerOperator)) return false;
    		
    		if(command.length > 3) {
    			new MessageManager(playerOperator).sendMessage("Invalid argument " + command[1], true);
    			return true;
    		}
    		
    		//don't look here, this is stupid
    		String[] temp = {command[2]};
    		int time = (new RollbackManager(temp,null,null)).getTime();
    		BlockListener.purgeDatabase(time);
    	}
    	List<String> helpAlias = configSektion.getStringList("blockLoggerCommands.help");
    	if(helpAlias.contains(command[1]) && PermissionManager.hasHelp(playerOperator)) {
    		//don't know if this is necessary 
    	}
    	return false;
	}

	/**
	 * This stores commands for the undo command
	 * @param operator the player who initiated the command
	 * @param command the type of logger command that was used
	 * @param arguments the arguments of that command
	 */
	private void storeCommand(String operator, String[] commandListed) {
		String command = "";
		for(String argument:commandListed) command = command+" " + argument;
		storedCommands.put(operator, commandListed);
	}
	/**
	 * This is the main logic done to convert a command to it's opposite, to then call the
	 * createLoggerManager with the newly created command. 
	 * 
	 * This approximately undoes the previous command. 
	 * @param player the player which command should be undone
	 * @param operator the player that initiated the command
	 * @return true if successful
	 */
	private boolean undoCommand(String player,String operator,Location location) {
		ConfigurationSection configSektion = ParamnesticCure.getInstance().getConfig().getConfigurationSection("");
		if(storedCommands.containsKey(player)) {
	       	String[] commandListed = storedCommands.get(player);
	       	
	       	List<String> rbAlias = configSektion.getStringList("blockLoggerCommands.rollback");
	       	List<String> reAlias = configSektion.getStringList("blockLoggerCommands.restore");
	       	if(rbAlias.contains(commandListed[1]))
	       		commandListed[1] = reAlias.get(0);
	       	else
	       		commandListed[1] = rbAlias.get(0);
	       	
	       	String command = "";
			for(String argument:commandListed) command = command+" " + argument;
			
			Player playerPlayer = Bukkit.getServer().getPlayer(player);
			//if the createLoggerManager does take the command as intercept
			if(!createLoggerManager(commandListed, location, playerPlayer))
				return false;
	       	storeCommand(operator,commandListed);
	       	return true;
	    }
		Player playerOperator = (operator == null)? null : Bukkit.getServer().getPlayer(operator);
		
		new MessageManager(playerOperator).sendMessage("Could not find the user " + player,true);
	    
		return false;
	}
}
