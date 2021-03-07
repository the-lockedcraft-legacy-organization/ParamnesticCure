/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.block;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.mcsmp.MessageManager;
import com.mcsmp.ParamnesticCure;

import net.coreprotect.CoreProtectAPI;


/**
 * An abstract class with methods that are shared between all loggerManagers, It also consist of some 
 * methods that are used to create those loggerManagers.
 * @author Thorin
 */
public abstract class LoggerManager {
	
	
	protected CoreProtectAPI coreprotect;
	protected int time;
	protected List<String> restrict_users;
	protected List<String> exclude_users;
	protected List<Object> restrict_blocks;
	protected List<Object> exclude_blocks;
	protected List<Integer> action_list;
	protected int radius;
	protected Location location;
	protected MessageManager msgManager;
	protected boolean isCancelled = false;
	protected boolean isIntercept = true;
	private static ConfigurationSection configSektion = ParamnesticCure.getInstance().getConfig().getConfigurationSection("");
	
	
	/**
	 * This function checks if any of the actions has been stored as creative and updates the creative status on that location
	 * @param actions
	 */
	protected int changeCreativeStatus(HashMap<ParamnesticLocation,Integer> actions){
		int creativeBlockCounter = 0;
		for(ParamnesticLocation actionKey: actions.keySet()) {
			try {
				Connection connection = ParamnesticCure.getInstance().getConnection();
				PreparedStatement statement = connection.prepareStatement(
		        		"SELECT is_creative FROM blockAction INNER JOIN worlds"
		        		+ " ON blockAction.world = worlds.world_id"
		        		+ " WHERE time = ? AND worlds.world = ? AND x = ? AND y = ? AND z = ?"
		        		+ " ORDER BY time DESC"
		        		);
				statement.setInt(1, actions.get(actionKey));
		        statement.setString(2,  actionKey.getWorld().getName());
		        statement.setInt(3, actionKey.getBlockX());
		        statement.setInt(4, actionKey.getBlockY());
		        statement.setInt(5, actionKey.getBlockZ());
		        
		        ResultSet set = statement.executeQuery();
		        boolean isCreative = false;
		        if(set.next()) 
		        	if(set.getInt(1) == 1)   // is_creative == 1 -> action was creative
		        		isCreative = true;
		        
		        ParamnesticCure.debug("LoggerManager.ChangeCreativeStatus", "Selected location " +  actionKey.getWorld().getName() + "," + actionKey.getBlockX() + "," + actionKey.getBlockY() + "," + actionKey.getBlockZ() + ",time=" + actions.get(actionKey));
		        creativeBlockCounter += actionKey.setCreativeStatus(isCreative);
        		statement.close();
		        connection.close();
			}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
		}
		return creativeBlockCounter;
	}
	/**
	 * Interprets the argument, and assigns values to the proper blocks
	 * @param arguments : The arguments of the command
	 * @param location : location where command was thrown
	 */
	protected void interpretArguments(String[] arguments, Location location) {
		
		
		boolean checkForWeirdUserInput = false;
		
		
    	for(String argument :arguments) {
			argument.replace(" ", "");
			if(isCancelled)
				break;
    		if(timeInterpreter(argument))
    			continue;
    		if(userInterpreter(argument))
    			continue;
    		if(excludeInterpreter(argument))
    			continue;
    		if(radiusInterpreter(argument,location))
    			continue;
    		if(blockInterpreter(argument))
    			continue;
    		if(actionInterpreter(argument))
    			continue;
    		if(argument != ""){
    			if(checkForWeirdUserInput) {
    	    		msgManager.sendMessage("Invalid argument, redirecting to logger",true);
    	    		isCancelled = true;
    	    		isIntercept = false;
    				return;
    			}
    			
    			checkForWeirdUserInput = true;
    			this.restrict_users = new ArrayList<String>();
    			this.restrict_users.add(argument);
    		}
    		
    	}
	}
	
	// ARGUMENT INTERPRETERS
	/**
	 * Checks if it's a time argument, if so assigns a time 
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean timeInterpreter(String argument) {
		List<String> timeAlias = configSektion.getStringList("blockLoggerCommands.arguments.time");
		argument = checkAndTrimArgument(argument,timeAlias);
    	if(argument != "") {
    		long time = 0;//s
    		
    		//might as well, right?
    		argument.replaceAll("seconds|second|sec", "s").replaceAll("minute|min","m").replaceAll("hours|hour", "h").replaceAll("days|day", "d").replaceAll("weeks|week", "w");
    		String[] splitedArgument = argument.split("(?<=[w*d*h*m*s])");
    		//convert to seconds
    		try {
	    		for(String part : splitedArgument) {
		    		if(part.contains("w")){ 
		    			time += 604800 * Double.parseDouble(part.substring( 0 , part.indexOf('w') ));
		    			continue;
		    		}
		    		if(part.contains("d")){ 
		    			time += 86400 * Double.parseDouble(part.substring( 0 , part.indexOf('d') ));
		    			continue;
		    		}
		    		if(part.contains("h")){ 
		    			time += 3600 * Double.parseDouble(part.substring( 0 , part.indexOf('h') ));
		    			continue;
		    		}
		    		if(part.contains("m")){ 
		    			time += 60 * Double.parseDouble(part.substring( 0 , part.indexOf('m') ));
		    			continue;
		    		}
		    		if(part.contains("s")){ 
		    			time += Double.parseDouble(part.substring( 0 , part.indexOf('s') ));
		    			continue;
		    		}
		    		//this is another way to specify seconds in Coreprotect
		    		time += Double.parseDouble(part);
		    		
	    		}
    		}catch(NumberFormatException e) {
    			msgManager.sendMessage("Invalid timeargument", true);
    			isCancelled = true;
    			}
    		
    		this.time = Math.round(time);
    		return true;
		}
		return false;
	}
	
	/**
	 * Checks if it's a user argument, if so assigns a list of all the users
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean userInterpreter(String argument) {
		List<String> userAlias = configSektion.getStringList("blockLoggerCommands.arguments.user");
		
		argument = checkAndTrimArgument(argument,userAlias);
    	if(argument != "") {
    		this.restrict_users = new ArrayList<String>();
    		
    		String[] argumentSplited = argument.split(",");
    		for(String part : argumentSplited) { 
    			this.restrict_users.add(part); 
    		}
    		return true;
    	}
		return false;
	}
	
	/**
	 * Checks if it's a exclude blocks argument, if so assigns a list of all the blocks that should be excluded
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean excludeInterpreter(String argument) {
		List<String> excludeAlias = configSektion.getStringList("blockLoggerCommands.arguments.exclude");
		argument = checkAndTrimArgument(argument,excludeAlias);
    	if(argument != "") {
    		exclude_blocks = blockStringListToBlockList(argument);
    		
    		return true;
    	}
		return false;
	}

	/**
	 * Checks if it's a block argument, if so assigns a list of all the blocks that should be selected
	 * @return true if there was a match
	 */
	private boolean blockInterpreter(String argument) {
		List<String> blockAlias = configSektion.getStringList("blockLoggerCommands.arguments.block");
		argument = checkAndTrimArgument(argument,blockAlias);
    	if(argument != "") {
    		restrict_blocks = blockStringListToBlockList(argument);
    		
    		return true;
    	}
		return false;
	}
	
	/**
	 * This should (ehm ehm) be able to convert argument into a list of blockdata
	 * @param argument
	 * @return A list with blockdatatypes
	 */
	private List<Object> blockStringListToBlockList(String argument){
		List<Object> tempBlockList = new ArrayList<Object>();
		if(argument.chars().allMatch(Character::isWhitespace))
			return tempBlockList;
		String[] argumentSplited = argument.split(",");
		for(String part : argumentSplited) {  
			try {
				tempBlockList.add( Bukkit.createBlockData(part) );  
			}catch(Exception e) {
				isIntercept = false;
			}
		}
		
		if(tempBlockList.size() == 0) {
			if(isIntercept)
				msgManager.sendMessage("No block specified", true);
			else{
				msgManager.sendMessage("Entity selection has not yet been implemented",true);
				msgManager.sendMessage("Cancelling intercept...",false);
			}
			isCancelled = true;
		}
		else if(!isIntercept){
			msgManager.sendMessage("Entity selection has not yet been implemented",true);
			msgManager.sendMessage("Cancelling intercept, paramnestic will still trigger...",false);
		}
		
		
		return tempBlockList;
	}
	
	/**
	 *  Checks if it's a radius argument, if so assigns radius and location of command
	 * @param argument
	 * @param radius_location the location where the command was issued, null => console
	 * @return true if there was a match
	 */
	private boolean radiusInterpreter(String argument,Location location) {
		List<String> radiusAlias = configSektion.getStringList("blockLoggerCommands.arguments.radius");
		argument = checkAndTrimArgument(argument,radiusAlias);
    	if(argument != "") {
    		if(location == null) {
    			msgManager.sendMessage("You can't use the radius argument from console",true);
    			isCancelled = true;
    		}
    		else {
    			try {
		    		this.radius = Integer.parseInt(argument); 
		    		this.location = location;
    			}catch(Exception e) {
    				msgManager.sendMessage("Invalid radius", true);
    				isCancelled = true;
    			}
    		}
    		return true;
		}
		return false;
	}
	
	
	/**
	 *  Checks if its a action argument, translates the actions into integer and puts them into a list of actions
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean actionInterpreter(String argument) {
		List<String> actionAlias = configSektion.getStringList("blockLoggerCommands.arguments.action");
		argument = checkAndTrimArgument(argument,actionAlias);
    	if(argument != "") {
    		
    		this.action_list = new ArrayList<Integer>();
    		String[] argumentSplited = argument.split(",");
    		
    		for(String actionArgument : argumentSplited) {
    			if(actionArgument.chars().allMatch(Character::isWhitespace))
    				break;
    			action_list.addAll(actionToInt(actionArgument));
    		}
    		if(isCancelled) {
    			if(isIntercept)
    				msgManager.sendMessage("No action specified", true);
    			else {
    				msgManager.sendMessage("Entity action selection not implemented yet",true);
    				msgManager.sendMessage("Cancelling intercept...",false);
    			}
    		}
    		return true;
    	}
		return false;
	}
	
	/**
	 * As of now, the only known action integers are block remove, block place and block interaction (0,1,2). 
	 * This just looks if its a block action, and with some hefty logic (if I say so myself) assigns the corresponding integers:
	 * block (0,1), +block(1), -block(0). Default value is 2
	 * @param action
	 * @return a list off all the actions that this part of the argument implied
	 */
	private List<Integer> actionToInt(String action) {
		//TODO make this fit better with coreprotect logic
		List<Integer> output = new ArrayList<Integer>();
		if(action.contains("block"))
		{
			if(!action.contains("-"))
				output.add(1);//place action
			if(!action.contains("+"))
				output.add(0);//remove action
		}
		else if(action.chars().allMatch(Character::isWhitespace)) {
			isCancelled = true;
		}
		else {
			isIntercept = false;
			isCancelled = true;
		}
		return output;
	}
	
	/**
	 * A function used in command processing, checks if the argument contains any alias and return the argument without its alias
	 * @param argument
	 * @param aliases
	 * @return The argument without alias, or "" if there was no match
	 */
	private String checkAndTrimArgument(String argument,List<String> aliases) {
		Matcher matcher;
		Pattern pattern = null;
		
		for(String alias : aliases) {

			pattern = Pattern.compile("^" + alias + ":");
			matcher = pattern.matcher(argument);
    		if(matcher.find()) {
    			return matcher.replaceAll("");
    		}
		}
		return "";
	}


    
	/**
	 * This function is shared between restores and rollbacks
	 */
	abstract boolean executeTask();
	/**
	 * @return the interpreted time
	 */
	public int getTime() {
		return time;
	}
	
}
