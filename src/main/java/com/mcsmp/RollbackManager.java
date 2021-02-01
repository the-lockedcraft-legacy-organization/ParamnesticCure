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
import java.util.HashMap;
import java.util.List;
import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Does a series of logical operations to minimise opportunities for rollblacks to mess with creative block data.
 * Note that this does not include rollback interference with inventories!
 * 
 * @author InteriorCamping
 * @author Thorin
 */
public class RollbackManager extends loggerManager{
	
	/**
     * Constructor for RollbackManager
     * @param arguments : The arguments of the command
     * @param radius_location : location where command was thrown
     */
    public RollbackManager(String[] arguments, Location radius_location) {
    	
    	this.coreprotect = ParamnesticCure.getInstance().getCoreProtect();
    	
    	interpretArguments(arguments,radius_location);
    }

    /**
     * This is all the logic that prevents any issues during a rollback event. A short summary:
     * 
     * Uses the CoreProtect API to get the block actions which is going to be rollbacked. 
     * Checks in the paramnestic database if any action that happened before the rollbacked event has been logged.
     * If that was the case and that was a creative blockplace event, add creative status to that location
     * Otherwise remove creative status
     * 
     * Also does the same logic as in a block break event to see if this current block needs to be stored in 
     * the paramnestic database
     */
    @Override
    public void executeTask() {
    	
    	ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {

			@Override
			public void run() {
				
				long startTime = System.nanoTime();
				
				List<String[]> affectedBlocksMsg = coreprotect.performRollback(
						time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, radius_location
		    			);
		    	
		    	long endTime = System.nanoTime();
		    	
		    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Operationall time: " + String.valueOf(  endTime-startTime  ) + " ns");
		    	
		    	try {
		    		
			    	int time; int x; int y; int z;
			    	String worldname; String playername; 
			    	
			    	
			    	
			    	
			    	
			    	
			    	for(String[] affectedBlockMsg : affectedBlocksMsg) {
			    		
			    		String msg = "";
			    		
			    		for(String temp : affectedBlockMsg) msg = msg + ":" + temp;//debug
			    		
			    		ParseResult affectedBlock = coreprotect.parseResult(affectedBlockMsg);
			    		
			    		if(affectedBlock.isRolledBack()) continue;
			    		
			    		
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
}