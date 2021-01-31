/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Does a series of logical operations to minimise opportunities for rollblacks to mess with creative block data.
 * Note that this does not include rollback interference with inventories!
 * 
 * @author InteriorCamping
 * @author Thorin
 */
public class RollbackManager {

    private CoreProtectAPI coreprotect;
	private int time;
	private List<String> restrict_users = null;
	private List<String> exclude_users = null;
	private List<Object> restrict_blocks = null;
	private List<Object> exclude_blocks = null;
	private List<Integer> action_list = null;
	private int radius;
	private Location radius_location = null;
	private static RollbackManager instance;
	/**
     * Constructor for RollbackManager
     * @param arguments ; The arguments of the command
     * @param radius_location ; location where command was thrown
     */
    public RollbackManager(String[] arguments, Location radius_location) {
    	
    	this.coreprotect = ParamnesticCure.getInstance().getCoreProtect();
    	
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
    	
    	
    	//For debug
    	String exclude_users = "";
    	if (this.exclude_users == null) exclude_users = "null";
    	else for(String user : this.exclude_users) exclude_users = exclude_users + "," + user;
    	
    	String restrict_users = "";
    	if (this.restrict_users == null) restrict_users = "null";
    	else for(String user : this.restrict_users) restrict_users = restrict_users + "," + user;
    	
    	String restrict_blocks = "";
    	if (this.restrict_blocks == null) restrict_blocks = "null";
    	else for(Object block : this.restrict_blocks) restrict_blocks = restrict_blocks + "," + block.toString();
    	
    	String exclude_blocks = "";
    	if (this.exclude_blocks == null) exclude_blocks = "null";
    	else for(Object block : this.restrict_blocks) exclude_blocks = exclude_blocks + "," + block.toString();
    	
    	String action_list = "";
    	if (this.action_list == null) action_list = "null";
    	else for(Integer number : this.action_list) action_list = action_list + "," + String.valueOf(number);
    	
    	String radius = String.valueOf(this.radius);
    	
    	String radius_location;
    	if (this.radius_location == null) radius_location = "null";
    	else radius_location = this.radius_location.toString();
    	
    	ParamnesticCure.getInstance().getLogger().info(
    			"[Manual Debug RollbackManager] Inputs: " + String.valueOf(this.time) + ":" + restrict_users + ":" + exclude_users + ":" +
    			restrict_blocks + ":" + exclude_blocks + ":" + action_list + ":" + radius + ":" + radius_location
    	);
    	//No longer debug
    	
    	instance = this;
    	
    	ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {

			@Override
			public void run() {
				
				RollbackManager RBmanager = RollbackManager.getInstance();
				long startTime = System.nanoTime();
				
				List<String[]> affectedBlocksMsg = RBmanager.coreprotect.performRollback(
						RBmanager.time, RBmanager.restrict_users, RBmanager.exclude_users, RBmanager.restrict_blocks, RBmanager.exclude_blocks,RBmanager.action_list, RBmanager.radius, RBmanager.radius_location
		    			);
		    	
		    	long endTime = System.nanoTime();
		    	
		    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Operationall time: " + String.valueOf(  endTime-startTime  ) + " ns");
		    	
		    	try {
		    		
			    	int time; int x; int y; int z; int worldID; int playerID; int creative;
			    	String worldname; String playername; 
			    	
			    	
			    	
			    	
			    	
			    	
			    	
			    	
			    	for(String[] affectedBlockMsg : affectedBlocksMsg) {
			    		String msg = "";
			    		
			    		for(String temp : affectedBlockMsg) msg = msg + ":" + temp;//debug
			    		
			    		ParseResult affectedBlock = coreprotect.parseResult(affectedBlockMsg);
			    		
			    		worldname = affectedBlock.worldName();
			    		playername = affectedBlock.getPlayer();
			    		time = affectedBlock.getTime();
			    		x = affectedBlock.getX();
			    		y = affectedBlock.getY();
			    		z = affectedBlock.getZ();

			        	Connection connection = ParamnesticCure.getInstance().getConnection();
	                    PreparedStatement getCreativeStatus = connection.prepareStatement(
	                    		"SELECT is_creative FROM blockAction"
	                    		+ " WHERE time < ? AND world = ? AND x = ? AND y = ? AND z = ?"
	                    		+ " ORDER BY time DESC"
	                    		); // Should return a list ordered by the most recent action that happened before the rollback
	                    getCreativeStatus.setInt(1, time);
	                    getCreativeStatus.setString(2, worldname);
	                    getCreativeStatus.setInt(3, x);
	                    getCreativeStatus.setInt(4, y);
	                    getCreativeStatus.setInt(5, z);
	                    
	                    ResultSet set = getCreativeStatus.executeQuery();
	                    
	                    List<World> worldlist = ParamnesticCure.getInstance().getServer().getWorlds();
			    		World world = null;
			    		
			    		for (World worldTest : worldlist) {
			    			if(worldTest.getName().equals(worldname)) { world = worldTest; break; }
			    		}
			    		
			    		
			    		
			    		Block block = world.getBlockAt(x, y, z);

			    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] block:" + block.toString() +", time:" + time);

			    		//if the block is creative, there would be problems when you undo rollbacks. This check prevents that
	                    TrackedBlocks.updateCreativeIDInDB(block);
	                    
	                    boolean hasNext = set.next();
	                    ParamnesticCure.getInstance().getLogger().info("[Manual Debug] hasNext: " + hasNext);
	                    
	                    if (hasNext && set.getInt(1) == 1) {
		                    RestrictedCreativeAPI.add(block);
		                    ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Block rollbacked to creative");
	                    }
	                    else {
	                    	RestrictedCreativeAPI.remove(block);
	                    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Block rollbacked to survival");
	                    }
	                    
			    	}
		    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
			}
    		
    	},60L);
    }
    
    public static RollbackManager getInstance() {
    	return instance;
    }
}