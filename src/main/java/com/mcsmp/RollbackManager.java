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
				
				long startTime = System.nanoTime(); //nano Seconds
				
				List<String[]> blockActionListMSG = coreprotect.performRollback(
						time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, radius_location
		    			);
		    	
		    	long endTime = System.nanoTime(); //nano Seconds
		    	
		    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Operationall time: " + String.valueOf(  (endTime-startTime)*Math.pow(10, -9)  ) + " s");
		    	
		    		
		    		
			    ParseResult blockAction = coreprotect.parseResult(blockActionListMSG.get(0));
			    String worldname = blockAction.worldName();
			    String playername = blockAction.getPlayer();
			    int oldestTime = blockAction.getTime();
			    	
			    int x = blockAction.getX(); 	int y = blockAction.getY(); 	int z = blockAction.getZ();
			    int newestTime = oldestTime;
			    
			    
			    for(int i = 0; i<blockActionListMSG.size(); i++){
			    	
			    	blockAction = coreprotect.parseResult(blockActionListMSG.get(i));
			    		
			    	if(blockAction.isRolledBack()) { continue; }
			    		
			    	if(i<blockActionListMSG.size()) {
				    	ParseResult nextBlockAction = coreprotect.parseResult(blockActionListMSG.get(i+1));
				    		
				    	// Scroll through database on the same location until the oldest action that is being rollbacked is the only one left
				    	if(	nextBlockAction.getX() == blockAction.getX()	 && 	nextBlockAction.getY() == blockAction.getY()	 && 	nextBlockAction.getZ() == blockAction.getZ()
				    			&& 		nextBlockAction.worldName() == blockAction.worldName()   &&    nextBlockAction.getTime() <= blockAction.getTime() 
				    			) { 
				    		
				    		if(newestTime < coreprotect.parseResult(blockActionListMSG.get(i)).getTime()) { newestTime = coreprotect.parseResult(blockActionListMSG.get(i)).getTime(); }
				    		continue;
			    		}
			    	}
			    	
			    	worldname = blockAction.worldName();
			    	playername = blockAction.getPlayer();
			    	oldestTime = blockAction.getTime(); newestTime = oldestTime;
			    		
			    	x = blockAction.getX(); 	y = blockAction.getY(); 	z = blockAction.getZ();
			    	
			    	int DBCreativeStatus = fetchDBIsCreative(oldestTime,worldname,x,y,z);

			        	
	                    
	                List<World> worldlist = ParamnesticCure.getInstance().getServer().getWorlds();
			    	World world = null;
			    		
			    	for (World worldTest : worldlist) {
			    		if(worldTest.getName().equals(worldname)) { world = worldTest; break; }
			    	}
			    		
			    		
			    		
			    	Block block = world.getBlockAt(x, y, z);
			    		
			    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] block:" + block.toString() +", time:" + oldestTime);

			    	//if the block is creative, there would be problems when you undo rollbacks. This check prevents that
			    	boolean iscreative = RestrictedCreativeAPI.isCreative(block);
			    	if(iscreative)
			    		TrackedBlocks.updateCreativeID(newestTime,playername,block,iscreative);
	                    
	                ParamnesticCure.getInstance().getLogger().info("[Manual Debug] hasNext: " + DBCreativeStatus);
	                    
	                if (DBCreativeStatus == 1) {
		                RestrictedCreativeAPI.add(block);
		                ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Block rollbacked to creative");
	                }
	                else {
	                   	RestrictedCreativeAPI.remove(block);
	                   	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Block rollbacked to survival");
	                }
			    }
			}
    		
    	},60L);
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
    private int fetchDBIsCreative(int time, String worldName, int x, int y, int z) {
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
}