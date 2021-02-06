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
import org.bukkit.configuration.ConfigurationSection;

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
	
	
	/**
	 * Interprets the argument, and assigns values to the proper blocks
	 * @param arguments : The arguments of the command
	 * @param radius_location : location where command was thrown
	 */
	protected void interpretArguments(String[] arguments, Location radius_location) {
		
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
    			this.restrict_users = new ArrayList<String>();
    			this.restrict_users.add(argument);
    		}
    		
    	}
	}

	/**
	 * Interprets a part of the command to see if a rollback,restore,undo, or purge should be created
	 * @param command The arguments after the logger alias
	 * @param location Location of the player (can be null)
	 * @param operator the player who initiated the command
	 * @return true if command event should be cancelled
	 */
	static public boolean createLoggerManager(String[] command, Location location, String operator) {
		

		//TODO Permissions
		
		ConfigurationSection configSektion = ParamnesticCure.getInstance().getConfig().getConfigurationSection("");
		List<String> rollbackAlias = configSektion.getStringList("blockLoggerCommands.rollback");
		String[] arguments = Arrays.copyOfRange(command, 2, command.length);
		
    	if(rollbackAlias.contains(command[1])) { 
    		
    		RollbackManager rollback = new RollbackManager( arguments , location  );
    		rollback.executeTask();
    		storeCommand(operator,command);
    		return true;
    	}
    	List<String> restoreAlias = configSektion.getStringList("blockLoggerCommands.restore");
    	if(restoreAlias.contains(command[1])) {
    		
    		
    		
    		RestoreManager restore = new RestoreManager(  arguments, location  );
    		restore.executeTask();
    		storeCommand(operator,command);
    		return true;
    	}
    	if(command[1].equals("undo")) {
    		//TODO make better undo's, that store location and takes time into consideration
    		String player = operator;
    		if(command.length == 3)
    			player = command[2];
    		else if(command.length > 3) {
    			ParamnesticCure.getInstance().getLogger().warning("Unkown amount of arguments");
    			return false;
    		}
    		if (undoCommand(player, operator, location))
    			return true;
    	}
    	if(command[1] == "purge") {
    		String world = null;
    		if(command.length == 4) 
    			world = command[3];
    		else if(command.length > 4) 
    			ParamnesticCure.getInstance().getLogger().warning("Unkown amount of arguments");
    		TrackedBlocks.purgeDatabase(Integer.parseInt(command[2]),world);
    	}
    	return false;
	}
	/**
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
	 * This is the main logic done to convert a command to it's opposite, to then call the createLoggerManager with the newly created command.
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
			
	       	createLoggerManager(commandListed, location, player);
	       	storeCommand(operator,commandListed);
	       	return true;
	    }else {
	       	ParamnesticCure.getInstance().getLogger().warning("No command from this user was found");
	    }
	        
		return false;
	}
	
	private boolean timeInterpreter(String argument) {
		String[] timeAlias = {"t:","time:"};
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
    		return true;
		}
		return false;
	}
	
	private boolean userInterpreter(String argument) {
		String[] userAlias = {"u:","user:","player:"};
		
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
		String[] excludeAlias = {"exclude:","e:"};
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
		String[] radiusAlias = {"r:","radius:", "area:"};
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
		String[] blockAlias = {"b:","block:"};
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
		String[] actionAlias = {"a:","action:"};
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
	 * @param argument
	 * @param aliases
	 * @return The argument without alias, or "" if there was no match
	 */
	private String checkAndTrimArgument(String argument, String[] aliases) {
		Matcher matcher;
		Pattern pattern = null;
		
		for(String alias : aliases)
			pattern = Pattern.compile("^" + alias);
			matcher = pattern.matcher(argument);
    		if(matcher.find()) {
    			return matcher.replaceAll("");
    		}
		return "";
	}
	
	
	
	abstract void executeTask();
	
	
}
