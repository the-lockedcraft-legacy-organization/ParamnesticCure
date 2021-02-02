package com.mcsmp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
		String argument;
    	
    	for(int i = 0; i < arguments.length ; i++) {
    		argument = arguments[i];
    		//check identifiers
    		//TODO Everything in here, this is way to repetitive
    		String[] actionAlias = {"a:","action:"};
    		for(String alias : actionAlias)
	    		if(argument.contains(alias)) {
	    			argument = argument.replaceAll(alias,"");
	    			//if this only was a identifier, then the next argument should be it's value, and that value should not be checked as if it were an identifier
	    			if(argument.length() == 0) { i++; argument = arguments[i]; }
	    			//interpret action argument into a list
	    			//TODO convert string identifiers to the right integer
	    			this.action_list = new ArrayList<Integer>();
	    			String[] argumentSplited = argument.split(",");
	    			for(String part : argumentSplited) {  this.action_list.add( Integer.parseInt(part) );  }
	    			continue;
    		}
    		String[] blockAlias = {"b:","block:"};
    		for(String alias : blockAlias)
	    		if(argument.contains(alias)) {
	    			argument = argument.replaceAll(alias,"");
	    			if(argument.length() == 0) { i++; argument = arguments[i]; }
	    			//interpret block argument into a list, then convert into material type
	    			this.restrict_blocks = new ArrayList<Object>();
	    			String[] argumentSplited = argument.split(",");
	    			for(String part : argumentSplited) {  this.restrict_blocks.add( Bukkit.createBlockData(part) );  }
	    			continue;
    		}
    		String[] excludeAlias = {"e:","exclude:"};
    		for(String alias : excludeAlias)
	    		if(argument.contains(alias)) {
	    			argument = argument.replaceAll(alias,"");
	    			if(argument.length() == 0) { i++; argument = arguments[i]; }
	    			
	    			this.exclude_blocks = new ArrayList<Object>();
	    			String[] argumentSplited = argument.split(",");
	    			for(String part : argumentSplited) {  this.exclude_blocks.add( Bukkit.createBlockData(part) );  }
	    			continue;
    		}
    		String[] radiusAlias = {"r:","radius:"};
    		for(String alias : radiusAlias)
	    		if(argument.contains(alias)) {
	    			argument = argument.replaceAll(alias,"");
	    			if(argument.length() == 0) { i++; argument = arguments[i]; }
	    			this.radius = Integer.parseInt(argument); 
	    			this.radius_location = radius_location;
	    			continue;
    		}
    		
    		String[] timeAlias = {"t:","time:"};
    		for(String alias : timeAlias)
	    		if(argument.contains(alias)){
	    			argument = argument.replaceAll(alias,"");
	    			if(argument.length() == 0) { i++; argument = arguments[i]; }
	    			
	    			int time = 0;//s
	    			
	    			argument.replace(",", "");
	    			
	    			
	    			String[] splitedArgument = argument.split("(?<=[w*d*h*m*s])");
	    			//convert to seconds
	    			//TODO make this work for 1d2w and 1d,2w
	    			for(String part : splitedArgument) {
	    				ParamnesticCure.getInstance().getLogger().info("[Manual Debug] part: " + part);
		    			if(part.contains("w")){ 
		    				time += 604800 * Integer.parseInt(part.substring( 0 , part.indexOf('w') ));
		    			}
		    			if(part.contains("d")){ 
		    				time += 86400 * Integer.parseInt(part.substring( 0 , part.indexOf('d') ));
		    			}
		    			if(part.contains("h")){ 
		    				time += 3600 * Integer.parseInt(part.substring( 0 , part.indexOf('h') ));
		    			}
		    			if(part.contains("m")){ 
		    				time += 60 * Integer.parseInt(part.substring( 0 , part.indexOf('m') ));
		    			}
		    			if(part.contains("s")){ 
		    				time += Integer.parseInt(part.substring( 0 , part.indexOf('s') ));
		    			}
	    			}
	    			this.time = time;
	    			continue;
	    		}
    		String[] userAlias = {"u:","user:"};
    		for(String alias : userAlias)
	    		if(argument.contains(alias)) {
	    			argument = argument.replaceAll(alias,"");
	    			if(argument.length() == 0) { i++; argument = arguments[i]; }
	    			
	    			this.restrict_users = new ArrayList<String>();
	    			
	    			String[] argumentSplited = argument.split(",");
	    			for(String part : argumentSplited) {  this.restrict_users.add(part);  }
	    			continue;
    		}
    		if(argument != ""){
    			this.restrict_users = new ArrayList<String>();
    			this.restrict_users.add(argument);
    		}
    		
    	}
	}

	
	
	
	abstract void executeTask();
}
