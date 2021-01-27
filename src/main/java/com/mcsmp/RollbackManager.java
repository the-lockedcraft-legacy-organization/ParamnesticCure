/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * @author InteriorCamping
 */

/**
 * Does a series of logical operations to minimize opportunities for rollblacks to mess with creative data.
 * Note that this does not include rollback interference with inventories!
 */
public class RollbackManager {

    
	private int time;
	private List<String> restrict_users;
	private List<String> exclude_users;
	private List<Object> restrict_blocks;
	private List<Object> exclude_blocks;
	private List<Integer> action_list;
	private int radius;
	private Location radius_location;
	
	/*
     * Constructor for Rollbacks
     * String[] arguments: the arguments of the rollback command
     */
    public RollbackManager(String[] arguments, Location radius_location) {
    	
    	this.restrict_users = new ArrayList<String>();
    	this.exclude_users = new ArrayList<String>();
    	this.restrict_blocks = new ArrayList<Object>();
    	this.exclude_blocks = new ArrayList<Object>();

    	String argument;
    	
    	for(int i = 0; i < arguments.length ; i++) {
    		argument = arguments[i];
    		//check identifiers
    		if(argument.contains("action:")) {
    			argument = argument.replaceAll("action:","");
    			//if this only was a identifier, then the next argument should be it's value, and that value should not be checked as if it were an identifier
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			//interpret action argument into a list
    			//not finished yet
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.action_list.add( Integer.parseInt(argument) );  }
    		}
    		else if(argument.contains("block:")) {
    			argument = argument.replaceAll("block:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			//interpret block argument into a list, then convert into material type
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.restrict_blocks.add( Bukkit.createBlockData(part) );  }
    		}
    		else if(argument.contains("exclude:")) {
    			argument = argument.replaceAll("exclude:","");
    			if(argument.length() == 0) { i++; argument = arguments[i]; }
    			
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
    			
    			int prevPos = 0;
    			String[] splitedArgument = argument.split(",");
    			
    			//convert to seconds (don't know any already existing functions that does this)
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
    			
    			
    			String[] argumentSplited = argument.split(",");
    			for(String part : argumentSplited) {  this.restrict_users.add(part);  }
    		}
    		else if(argument != ""){
    			this.restrict_users.add(argument);
    		}
    		
    	}
    }

    // ┌─────────────────────────────────────────── /!\=- 𝗪𝗔𝗥𝗡𝗜𝗡𝗚 /!\ ─────────────────────────────────────────
    // │
    // │  𝘛𝘩𝘪𝘴 𝘪𝘴 𝘢 𝘩𝘪𝘨𝘩𝘭𝘺 𝘤𝘰𝘮𝘱𝘭𝘦𝘹 𝘰𝘱𝘦𝘳𝘢𝘵𝘪𝘰𝘯! 𝘐𝘵 𝘤𝘰𝘯𝘴𝘪𝘥𝘦𝘳𝘴 𝘵𝘩𝘳𝘦𝘦 𝘥𝘦𝘨𝘳𝘦𝘦𝘴 𝘰𝘧 𝘣𝘭𝘰𝘤𝘬 𝘰𝘱𝘦𝘳𝘢𝘵𝘪𝘰𝘯𝘴 (𝘵𝘸𝘦𝘭𝘷𝘦 𝘴𝘵𝘢𝘵𝘦𝘴 𝘪𝘯 𝘵𝘰𝘵𝘢𝘭!)
    // │ 𝘖𝘯𝘭𝘺 𝘵𝘰𝘶𝘤𝘩 𝘵𝘩𝘪𝘴 𝘭𝘰𝘨𝘪𝘤 𝘪𝘧 𝘺𝘰𝘶 𝘩𝘢𝘷𝘦 𝘢 𝘴𝘵𝘳𝘰𝘯𝘨 𝘶𝘯𝘥𝘦𝘳𝘴𝘵𝘢𝘯𝘥𝘪𝘯𝘨 𝘰𝘧 𝘱𝘦𝘳𝘮𝘶𝘵𝘢𝘵𝘪𝘰𝘯𝘴 𝘢𝘯𝘥 𝘮𝘢𝘯𝘺 𝘩𝘰𝘶𝘳𝘴 𝘵𝘰 𝘵𝘦𝘴𝘵 𝘺𝘰𝘶𝘳 𝘤𝘩𝘢𝘯𝘨𝘦𝘴!
    // │
    // │           𝗘𝘃𝗲𝗻 𝗼𝗻𝗲 𝘀𝗺𝗮𝗹𝗹 𝗰𝗵𝗮𝗻𝗴𝗲 𝘁𝗼 𝘁𝗵𝗶𝘀 𝘀𝗲𝗰𝘁𝗶𝗼𝗻 𝗶𝘀 𝗲𝗻𝗼𝘂𝗴𝗵 𝘁𝗼 𝗺𝗲𝘀𝘀 𝘁𝗵𝗲 𝘄𝗵𝗼𝗹𝗲 𝘁𝗵𝗶𝗻𝗴 𝘂𝗽!
    // │
    // └──────────────────────────────────────────────────────────────────────────────────────────────────────────
    /*
     * Performs a series of logical operations to determine if the blocks getting rolled back should be protected by creative mode.
     */
    public void executeTask() {
        ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("coreprotect").getDatabase().getConnection();
                    //co_world
                    PreparedStatement statement = connection.prepareStatement(
                    		"SELECT * from co_block,co_world"
                    		+ " INNER JOIN co_world"
                    		+ " ON co_block.wid=co_world.id");
                    
                    ResultSet set = statement.executeQuery();
                    while(set.next()){
                        int action = set.getInt("action");
                        Location location = new Location(ParamnesticCure.getInstance().getServer().getWorld(set.getString("world")), set.getInt("x"), set.getInt("y"), set.getInt("z"));
                        
                        if(set.getInt("rollback") > 0) {
                            switch(action) {
                                case 0:
                                    if(ParamnesticCure.getInstance().getTrackedBlocks().getBlockList().containsKey(location)) {
                                        ParamnesticCure.getInstance().getTrackedBlocks().removeFromBlockList(location);
                                        RestrictedCreativeAPI.add(location.getBlock());
                                    }
                                    break;
                                case 1:
                                        ParamnesticCure.getInstance().getTrackedBlocks().addToBlockList(location);
                                        RestrictedCreativeAPI.remove(location.getBlock());
                                    break;
                                default: break;
                            }
                        } 
                        else {
                            switch(action) {
                                case 0:
                                        ParamnesticCure.getInstance().getTrackedBlocks().addToBlockList(location);
                                        RestrictedCreativeAPI.remove(location.getBlock());
                                    break;
                                case 1:
                                    if(ParamnesticCure.getInstance().getTrackedBlocks().getBlockList().containsKey(location)) {
                                        ParamnesticCure.getInstance().getTrackedBlocks().removeFromBlockList(location);
                                        if(!(location.getBlock().isEmpty() || location.getBlock().isLiquid())) {
                                            RestrictedCreativeAPI.add(location.getBlock());
                                        }
                                    }
                                    break;
                                default: break;
                            }
                        }
                        
                    } 
                    
                    
                } catch (SQLException ex) {

                }
            }
        }, 60L);
    }
}