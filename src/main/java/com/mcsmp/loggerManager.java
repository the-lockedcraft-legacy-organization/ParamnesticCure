package com.mcsmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import net.coreprotect.CoreProtectAPI;


/**
 * An abstract class with methods that are shared between all loggerManagers, It also consist of some 
 * methods that are used to create those loggerManagers
 * @author Thorin
 */
public abstract class loggerManager {
	
	
	protected CoreProtectAPI coreprotect;
	protected int time;
	protected List<String> restrict_users;
	protected List<String> exclude_users;
	protected List<Object> restrict_blocks;
	protected List<Object> exclude_blocks;
	protected List<Integer> action_list;
	protected int radius;
	protected Location radius_location;
	private static HashMap<String,String[]> storedCommands = new HashMap<String,String[]>();
	private static ConfigurationSection configSektion = ParamnesticCure.getInstance().getConfig().getConfigurationSection("");
	
	
	/**
	 * Interprets the argument, and assigns values to the proper blocks
	 * @param arguments : The arguments of the command
	 * @param radius_location : location where command was thrown
	 */
	protected void interpretArguments(String[] arguments, Location radius_location) {
		
		
		boolean checkForWeirdUserInput = false;
		
		
    	for(String argument :arguments) {
    		
    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] " + argument);
			argument.replace(" ", "");
			
    		if(timeInterpreter(argument))
    			continue;
    		if(userInterpreter(argument))
    			continue;
    		if(excludeInterpreter(argument))
    			continue;
    		if(radiusInterpreter(argument,radius_location))
    			continue;
    		if(blockInterpreter(argument))
    			continue;
    		if(actionInterpreter(argument))
    			continue;
    		if(argument != ""){
    			if(checkForWeirdUserInput) {
    	    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Weird user input detected");
    				return;
    			}
    			
    			checkForWeirdUserInput = true;
    			this.restrict_users = new ArrayList<String>();
    			this.restrict_users.add(argument);
    		}
    		
    	}
	}
	
	/**
	 * Takes an action as input, then calls a function which adds the block into the
	 *  database (an important function if you want restores to work properly).
	 * 
	 * It also checks the creative status from paramnestic database of the specified
	 * action, to then set the creative status of that block accordingly.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param worldname
	 * @param time
	 */
	protected void changeCreativeStatus(int x, int y, int z, String worldname,int time) {
		
		
		int DBCreativeStatus = fetchDBIsCreative( time,  worldname,  x,  y,  z);
		
		
		List<World> worldlist = ParamnesticCure.getInstance().getServer().getWorlds();
    	World world = null;
    	for (World worldTest : worldlist) {
    		if(worldTest.getName().equals(worldname)) { world = worldTest; break; }
    	}
    	Block block = world.getBlockAt(x, y, z);
    	
    	//to prevent issues on restores
    	if(RestrictedCreativeAPI.isCreative(block)) {
    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] stored a block as creative");
    		TrackedBlocks.updateCreativeID(block, true);
    	}
    	
		if(DBCreativeStatus == 1) {
			RestrictedCreativeAPI.add(block);
			ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Block restored to creative");
		}
		else {
			RestrictedCreativeAPI.remove(block);
			ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Block restored to survival");
		}
	} 
	/**
	 * Interprets a part of the command to see if a rollback,restore,undo, or purge should be created
	 * Whether this method fits better in the listener class is unclear
	 * @param command The arguments after the logger alias
	 * @param location Location of the player (can be null)
	 * @param playerOperator the player who initiated the command
	 * @return true if command event should be cancelled
	 */
	static public boolean createLoggerManager(String[] command, Location location, Player playerOperator) {
		
		String operator = (playerOperator == null) ? null : playerOperator.getName();
		
		
		List<String> rollbackAlias = configSektion.getStringList("blockLoggerCommands.rollback");
		String[] arguments = Arrays.copyOfRange(command, 2, command.length);
		
    	if(rollbackAlias.contains(command[1])) { 
    		if(!PermissionManager.hasRollback(playerOperator)) return false;
    		
    		
    		RollbackManager rollback = new RollbackManager( arguments , location  );
    		rollback.executeTask();
    		storeCommand(operator,command);
    		return true;
    	}
    	List<String> restoreAlias = configSektion.getStringList("blockLoggerCommands.restore");
    	if(restoreAlias.contains(command[1])) {
    		
    		if(!PermissionManager.hasRestore(playerOperator)) return false;
    		
    		
    		RestoreManager restore = new RestoreManager(  arguments, location  );
    		restore.executeTask();
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
    			ParamnesticCure.getInstance().getLogger().warning("Unkown amount of arguments");
    			return false;
    		}
    		
    		//don't look here, this is stupid
    		String[] temp = {command[2]};
    		int time = (new RollbackManager(temp,null)).time;
    		TrackedBlocks.purgeDatabase(time);
    	}
    	List<String> helpAlias = configSektion.getStringList("blockLoggerCommands.help");
    	if(helpAlias.contains(command[1])) {
    		if(!PermissionManager.hasHelp(playerOperator)) return false;
	    	
    		
    	}
    	return false;
	}
	/**
	 * This stores commands for the undo command
	 * @param operator the player who initiated the command
	 * @param command the type of logger command that was used
	 * @param arguments the arguments of that command
	 */
	static private void storeCommand(String operator, String[] commandListed) {
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
	static private boolean undoCommand(String player,String operator,Location location) {
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
			
	       	createLoggerManager(commandListed, location, playerPlayer);
	       	storeCommand(operator,commandListed);
	       	return true;
	    }else {
	       	ParamnesticCure.getInstance().getLogger().warning("No command from this user was found");
	    }
	        
		return false;
	}
	
	/*
	 * Argument interpreters:
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
	    		}
	    		if(part.contains("d")){ 
	    			time += 86400 * Double.parseDouble(part.substring( 0 , part.indexOf('d') ));
	    		}
	    		if(part.contains("h")){ 
	    			time += 3600 * Double.parseDouble(part.substring( 0 , part.indexOf('h') ));
	    		}
	    		if(part.contains("m")){ 
	    			time += 60 * Double.parseDouble(part.substring( 0 , part.indexOf('m') ));
	    		}
	    		if(part.contains("s")){ 
	    			time += Double.parseDouble(part.substring( 0 , part.indexOf('s') ));
	    		}
    		}
    		this.time = Math.round(time);
    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] time = " + String.valueOf(this.time));
    		return true;
		}
		return false;
	}
	
	private boolean userInterpreter(String argument) {
		List<String> userAlias = configSektion.getStringList("blockLoggerCommands.arguments.user");
		
		argument = checkAndTrimArgument(argument,userAlias);
    	if(argument != "") {
    			
    		this.restrict_users = new ArrayList<String>();
    		
    		String[] argumentSplited = argument.split(",");
    		for(String part : argumentSplited) {  this.restrict_users.add(part);  }
    		return true;
    		}
		return false;
	}
	
	private boolean excludeInterpreter(String argument) {
		List<String> excludeAlias = configSektion.getStringList("blockLoggerCommands.arguments.exclude");
		argument = checkAndTrimArgument(argument,excludeAlias);
    	if(argument != "") {
    			
    		this.exclude_blocks = new ArrayList<Object>();
    		String[] argumentSplited = argument.split(",");
    		for(String part : argumentSplited) {  this.exclude_blocks.add( Bukkit.createBlockData(part) );  }
    		return true;
    		}
		return false;
	}
	
	private boolean radiusInterpreter(String argument,Location radius_location) {
		List<String> radiusAlias = configSektion.getStringList("blockLoggerCommands.arguments.radius");
		argument = checkAndTrimArgument(argument,radiusAlias);
    	if(argument != "") {
    		if(radius_location == null)
    			ParamnesticCure.getInstance().getLogger().warning(" You can't use the radius argument from the console");
    		this.radius = Integer.parseInt(argument); 
    		this.radius_location = radius_location;
    		return true;
		}
		return false;
	}
	
	private boolean blockInterpreter(String argument) {
		List<String> blockAlias = configSektion.getStringList("blockLoggerCommands.arguments.block");
		argument = checkAndTrimArgument(argument,blockAlias);
    	if(argument != "") {
    			
    		this.restrict_blocks = new ArrayList<Object>();
    		String[] argumentSplited = argument.split(",");
    		
    		for(String material : argumentSplited) {  
    			this.restrict_blocks.add( Material.getMaterial("material:" + material) );  
    			}
    		return true;
    		}
		return false;
	}
	
	private boolean actionInterpreter(String argument) {
		List<String> actionAlias = configSektion.getStringList("blockLoggerCommands.arguments.action");
		argument = checkAndTrimArgument(argument,actionAlias);
    	if(argument != "") {
    		
    		this.action_list = new ArrayList<Integer>();
    		String[] argumentSplited = argument.split(",");
    		for(String actionArgument : argumentSplited) {
    			for(Integer action:actionToInt(actionArgument))
    				action_list.add(action);
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
	 * @return
	 */
	private List<Integer> actionToInt(String action) {
		List<Integer> output = null;
		if(action.contains("block"))
		{
			output = new ArrayList<Integer>();
			if(!action.contains("-"))
				output.add(1);//place action
			if(!action.contains("+"))
				output.add(0);//remove action
		}
		else {
			output = new ArrayList<Integer>();
			output.add(2);
		}
		return output;
	}
	
	/**
	 * A function used in command processing
	 * @param argument
	 * @param aliases
	 * @return The argument without alias, or "" if there was no match
	 */
	private String checkAndTrimArgument(String argument,List<String> aliases) {
		Matcher matcher;
		Pattern pattern = null;
		
		for(String alias : aliases) {

    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] checking Alias: " + alias);
			pattern = Pattern.compile("^" + alias + ":");
			matcher = pattern.matcher(argument);
    		if(matcher.find()) {
    			ParamnesticCure.getInstance().getLogger().info("[Manual Debug] There was a match");
    			return matcher.replaceAll("");
    		}
		}
		return "";
	}

	
	abstract int fetchDBIsCreative(int time, String worldName, int x, int y, int z);
	
	abstract void executeTask();
	
	
}
