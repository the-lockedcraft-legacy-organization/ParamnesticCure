package com.mcsmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.coreprotect.CoreProtectAPI;


/**
 * An abstract class with methods that are shared between all loggerManagers
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

	static public void createLoggerManager(String[] arguments, Location location) {
		

		//TODO Permissions
		
		ConfigurationSection configSektion = ParamnesticCure.getInstance().getConfig().getConfigurationSection("");
		List<String> rollbackAlias = configSektion.getStringList("blockLoggerCommands.rollback");
    	if(rollbackAlias.contains(arguments[1])) { 
    		RollbackManager rollback = new RollbackManager(  Arrays.copyOfRange(arguments, 2, arguments.length), location  );
    		rollback.executeTask();
    		return;
    	}
    	List<String> restoreAlias = configSektion.getStringList("blockLoggerCommands.restore");
    	if(restoreAlias.contains(arguments[1])) {
    		RestoreManager restore = new RestoreManager(  Arrays.copyOfRange(arguments, 2, arguments.length), location  );
    		restore.executeTask();
    		return;
    	}
    	if(arguments[1] == "undo") {
    		//TODO put code in here
    		return;
    	}
    	if(arguments[1] == "purge") {
    		//TODO put code in here
    		return;
    	}
    	
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
    		
    		//interpret action argument into a list
    		//TODO convert string identifiers to the right integer
    		this.action_list = new ArrayList<Integer>();
    		String[] argumentSplited = argument.split(",");
    		for(String action : argumentSplited) {  
    			this.action_list.add(actionToInt(action));
    		}
    		return true;
    	}
		return false;
	}
	
	private int actionToInt(String action) {
		//This is tedious...
		int a = 0;
		if(action.contains("block"))
		{}
		return a;
	}
	
	/**
	 * 
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
