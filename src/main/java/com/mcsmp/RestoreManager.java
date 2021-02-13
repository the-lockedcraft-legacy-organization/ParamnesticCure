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
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.coreprotect.CoreProtectAPI.ParseResult;
/**
 * Does a series of logical operations to minimise opportunities for restores to mess with creative block data.
 * Note that this does not include restore interference with inventories!
 * 
 * @author Thorin
 */
public class RestoreManager extends loggerManager {
	/**
     * Constructor for RestoreManager
     * @param arguments : The arguments of the command
     * @param radius_location : location where command was thrown
     */
	public RestoreManager(String[] arguments, Location radius_location,Player player) {
		
		this.msgManager = new MessageManager(player,"Restore");
		this.coreprotect = ParamnesticCure.getInstance().getCoreProtect();
    	
    	interpretArguments(arguments,radius_location);
	}
	
	@Override
	public void executeTask() {
		ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {

				@Override
				public void run() {
					long startTime = System.nanoTime();
					
					List<String[]> blockActionListMSG = coreprotect.performRestore(
							time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, radius_location
			    			);
			    	
			    	long endTime = System.nanoTime();
			    	
			    	msgManager.sendMessage( "Operationall time: " + String.valueOf( (endTime-startTime)*Math.pow(10, -9) ) , false);
			    	
			    	
			    	if(blockActionListMSG.size() == 0) {
			    		msgManager.sendMessage("No actions were found",true);
			    		return;
			    	}
			    	
			    	
			    	String worldname;
			    	int x; 	int y; 	int z;
			    	int newestTime;
			    	ParseResult blockAction;
			    	
			    	HashMap<String,Integer> blocks_to_be_changed = new HashMap<String,Integer>();
			    	
			    	for(String[] parseMSG : blockActionListMSG) {
			    		blockAction = coreprotect.parseResult(parseMSG);
			    		
			    		if(!blockAction.isRolledBack())	continue;

			    		x = blockAction.getX(); 	y = blockAction.getY(); 	z = blockAction.getZ();
				    	newestTime = blockAction.getTime(); worldname = blockAction.worldName();
				    	
				    	String compareKey = String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + worldname;
			    		if(blocks_to_be_changed.containsKey(compareKey))
			    			if(blocks_to_be_changed.get(compareKey) > newestTime)
			    				continue;
			    		blocks_to_be_changed.put( compareKey , newestTime );
			    		
			    		
			    		
			    		
				    	changeCreativeStatus(x,y,z,worldname,newestTime);
			    	}
			    	
			    	msgManager.sendMessage( String.valueOf(blockActionListMSG.size()) + " block actions were found, " + String.valueOf( blocks_to_be_changed.size() ) + " Blocks were set", false);
				}
			
				
				
		},60L);
	}
	/**
     * Returns the creative status on the specified action
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
        		+ " WHERE time = ? AND world = ? AND x = ? AND y = ? AND z = ?"
        		+ " ORDER BY time DESC"
        		);
        getCreativeStatus.setInt(1, time);
        getCreativeStatus.setInt(2, WorldManager.getWorldId( worldName ));
        getCreativeStatus.setInt(3, x);
        getCreativeStatus.setInt(4, y);
        getCreativeStatus.setInt(5, z);
        
        
        
        ResultSet set = getCreativeStatus.executeQuery();
        if(set.next()) return set.getInt(1);
    	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	
    	return -1;
    }

}
