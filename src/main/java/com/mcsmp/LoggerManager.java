/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;
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
     * Checks the database if this action has been stored as creative
     * @param time The time of action
     * @param worldName
     * @param x
     * @param y
     * @param z
     * @return true if a creative action at this time and location was stored in the database
     */
    protected boolean fetchDBIsCreative(int time, String worldName, int x, int y, int z) {
    	try {
    	Connection connection = ParamnesticCure.getInstance().getConnection();
        PreparedStatement getCreativeStatus = connection.prepareStatement(
        		"SELECT is_creative FROM blockAction INNER JOIN worlds"
        		+ " ON blockAction.world = worlds.world_id"
        		+ " WHERE time = ? AND worlds.world = ? AND x = ? AND y = ? AND z = ?"
        		+ " ORDER BY time DESC"
        		);
        getCreativeStatus.setInt(1, time);
        getCreativeStatus.setString(2, worldName );
        getCreativeStatus.setInt(3, x);
        getCreativeStatus.setInt(4, y);
        getCreativeStatus.setInt(5, z);
        
        ResultSet set = getCreativeStatus.executeQuery();
        if(set.next()) return (set.getInt(1) == 1); // is_creative = 1 -> action was creative
    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	
    	return false;
    }
	
	/**
	 * Takes an action as input, then calls a function which adds the block into the
	 *  database (an important function if you want restores to work properly).
	 * 
	 * It also updates the creative status on the location
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param worldname
	 * @param DBCreativeStatus true if this location should be set to creative
	 */
	protected void changeCreativeStatus(Integer x, Integer y, Integer z, String worldname, boolean DBCreativeStatus) {
		
		
		List<World> worldlist = ParamnesticCure.getInstance().getServer().getWorlds();
    	World world = null;
    	for (World worldTest : worldlist) {
    		if(worldTest.getName().equals(worldname)) { world = worldTest; break; }
    	}
    	Block block = world.getBlockAt(x, y, z);
    	
    	//to prevent issues on restores as some actions would not have been tracked otherways
    	if(RestrictedCreativeAPI.isCreative(block)) {
    		BlockTracker.updateCreativeID(block, true);
    	}
    	
		if(DBCreativeStatus) {
			RestrictedCreativeAPI.add(block);
			
		}
		else {
			RestrictedCreativeAPI.remove(block);
		}
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
	 * 
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean timeInterpreter(String argument) {
		List<String> timeAlias = configSektion.getStringList("blockLoggerCommands.arguments.time");
		argument = checkAndTrimArgument(argument,timeAlias);
    	if(argument != "") {
    		long time = 0;//s
    		
    		
    		
    		String[] splitedArgument = argument.split("(?<=[w*d*h*m*s])");
    		//convert to seconds
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
	    		try {
	    			//this is another way to specify seconds in Coreprotect
	    			time += Double.parseDouble(part);
	    		}catch(Exception e) {
	    			msgManager.sendMessage("Incorrect timeargument, redirecting to blocklogger...", true);
	    			isCancelled = true;
	    			isIntercept = false;
	    			break;
	    		}
    		}
    		this.time = Math.round(time);
    		return true;
		}
		return false;
	}
	
	/**
	 * 
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
	 * 
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean excludeInterpreter(String argument) {
		List<String> excludeAlias = configSektion.getStringList("blockLoggerCommands.arguments.exclude");
		argument = checkAndTrimArgument(argument,excludeAlias);
    	if(argument != "") {
    		exclude_blocks = blockStringListToBlockList(argument);
    		if(exclude_blocks.size() > 0)
    			return true;
    	}
		return false;
	}

	/**
	 * 
	 * @param argument
	 * @return true if there was a match
	 */
	private boolean blockInterpreter(String argument) {
		List<String> blockAlias = configSektion.getStringList("blockLoggerCommands.arguments.block");
		argument = checkAndTrimArgument(argument,blockAlias);
    	if(argument != "") {
    		restrict_blocks = blockStringListToBlockList(argument);
    		if(restrict_blocks.size() == 0) {
    			if(isIntercept)
    				msgManager.sendMessage("Missing action arguments", true);
    			else{
    				msgManager.sendMessage("Unimplemented command detected",true);
    				msgManager.sendMessage("Triggering command for logger...",false);
    			}
    			isCancelled = true;
    		}
    		else if(!isIntercept){
    			msgManager.sendMessage("Partially implemented command detected",true);
    			msgManager.sendMessage("Triggering command for paramnestic and logger...",false);
    		}
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
		return tempBlockList;
	}
	
	/**
	 * 
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
    		this.radius = Integer.parseInt(argument); 
    		this.location = location;
    		}
    		return true;
		}
		return false;
	}
	
	
	/**
	 * 
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
    			for(Integer action : actionToInt(actionArgument))
    				action_list.add(action);
    		}
    		if(isCancelled) {
    			if(isIntercept)
    				msgManager.sendMessage("Missing action arguments", true);
    			else {
    				msgManager.sendMessage("Unimplemented command detected",true);
    				msgManager.sendMessage("Triggering command for logger...",false);
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
	
	
}
