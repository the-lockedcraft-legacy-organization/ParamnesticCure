package com.mcsmp;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    		if(argument.contains("action:")) {
    			argument = argument.replaceAll("action:","");
    			//if this only was a identifier, then the next argument should be it's value, and that value should not be checked as if it were an identifier
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			//interpret action argument into a list
    			//TODO convert string identifiers to the right integer
    			this.action_list = new ArrayList<Integer>();
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.action_list.add( Integer.parseInt(part) );  }
    		}
    		else if(argument.contains("block:")) {
    			argument = argument.replaceAll("block:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			//interpret block argument into a list, then convert into material type
    			this.restrict_blocks = new ArrayList<Object>();
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.restrict_blocks.add( Bukkit.createBlockData(part) );  }
    		}
    		else if(argument.contains("exclude:")) {
    			argument = argument.replaceAll("exclude:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			
    			this.exclude_blocks = new ArrayList<Object>();
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.exclude_blocks.add( Bukkit.createBlockData(part) );  }
    		}
    		else if(argument.contains("radius:")) {
    			argument = argument.replaceAll("radius:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			this.radius = Integer.parseInt(argument); 
    			this.radius_location = radius_location;
    		}
    		else if(argument.contains("time:")){
    			argument = argument.replaceAll("time:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			
    			int time = 0;//s
    			
    			argument.replace(",", "");
    			
    			
    			
    			
    			
    			
    			
    			String[] splitedArgument = argument.split(",");
    			
    			//convert to seconds
    			//TODO make this to work for 1d2w and 1d,2w
    			for(String part : splitedArgument) {
	    			if(argument.contains("w")){ 
	    				time += 604800 * Integer.parseInt(part.substring( 0 , part.indexOf('w') ));
	    			}
	    			if(argument.contains("d")){ 
	    				time += 86400 * Integer.parseInt(part.substring( 0 , part.indexOf('d') ));
	    			}
	    			if(argument.contains("h")){ 
	    				time += 3600 * Integer.parseInt(part.substring( 0 , part.indexOf('h') ));
	    			}
	    			if(argument.contains("m")){ 
	    				time += 60 * Integer.parseInt(part.substring( 0 , part.indexOf('m') ));
	    			}
	    			if(argument.contains("s")){ 
	    				time += Integer.parseInt(part.substring( 0 , part.indexOf('s') ));
	    			}
    			}
    			this.time = time;
    		}
    		else if(argument.contains("user:")) {
    			argument = argument.replaceAll("user:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			
    			this.restrict_users = new ArrayList<String>();
    			
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.restrict_users.add(part);  }
    		}
    		else if(argument != ""){
    			this.restrict_users = new ArrayList<String>();
    			this.restrict_users.add(argument);
    		}
    		
    	}
	}
	/**
     * Returns the creative status on the action before the specified action
     * @param time
     * @param worldName
     * @param x
     * @param y
     * @param z
     * @return boolean: [0 1] | not in database: -1
     */
	protected int fetchDBIsCreative(int time, String worldName, int x, int y, int z) {
    	try {
    	Connection connection = ParamnesticCure.getInstance().getConnection();
        PreparedStatement getCreativeStatus = connection.prepareStatement(
        		"SELECT is_creative FROM blockAction"
        		+ " WHERE time < ? AND world = ? AND x = ? AND y = ? AND z = ?"
        		+ " ORDER BY time DESC"
        		);
        getCreativeStatus.setInt(1, time);
        getCreativeStatus.setString(2, worldName);
        getCreativeStatus.setInt(3, x);
        getCreativeStatus.setInt(4, y);
        getCreativeStatus.setInt(5, z);
        
        ResultSet set = getCreativeStatus.executeQuery();
        if(set.next()) return set.getInt(1);
    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	
    	return -1;
    }
	
	abstract void executeTask();
}
