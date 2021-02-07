package com.mcsmp;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;
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
	public RestoreManager(String[] arguments, Location radius_location) {
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
			    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Operationall time: " + String.valueOf(  endTime-startTime  ) + " ns");
			    	
			    	
			    	if(blockActionListMSG.size() == 0) {
			    		ParamnesticCure.getInstance().getLogger().warning("No actions were found");
			    		return;
			    	}
			    	
			    	ParseResult blockAction = coreprotect.parseResult(blockActionListMSG.get(0));
			    	
			    	String worldname = blockAction.worldName();
			    	int x = blockAction.getX(); 	int y = blockAction.getY(); 	int z = blockAction.getZ();
			    	int newestTime = blockAction.getTime();
			    	
			    	
			    	HashMap<String,Integer> blocks_to_be_changed = new HashMap<String,Integer>();
			    	
			    	for(int i = 0; i < blockActionListMSG.size(); i++) {
			    		blockAction = coreprotect.parseResult(blockActionListMSG.get(i));
			    		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] x=" + blockAction.getX() + ", y=" + blockAction.getY() + " ,z=" + blockAction.getZ() + " ,time=" + blockAction.getTime() + " ,rollback=" + blockAction.isRolledBack());
			    		
			    		if(!blockAction.isRolledBack())	continue;
			    		
				    	
				    	String compareKey = String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + worldname;
			    		if(blocks_to_be_changed.containsKey(compareKey))
			    			if(blocks_to_be_changed.get(compareKey) > blockAction.getTime())
			    				continue;
			    		blocks_to_be_changed.put( compareKey , newestTime );
			    		
			    		
			    		x = blockAction.getX(); 	y = blockAction.getY(); 	z = blockAction.getZ();
				    	newestTime = blockAction.getTime(); worldname = blockAction.worldName();
			    		
				    	changeCreativeStatus(x,y,z,worldname,newestTime);
			    	}
			    	
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
		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] time = " + String.valueOf(time) + " ,worldName" + worldName + " ,x = " + String.valueOf(x) + " ,y = " + String.valueOf(y) + " ,z=" + String.valueOf(z));
		
    	try {
    	Connection connection = ParamnesticCure.getInstance().getConnection();
        PreparedStatement getCreativeStatus = connection.prepareStatement(
        		"SELECT is_creative FROM blockAction"
        		+ " WHERE time = ? AND world = ? AND x = ? AND y = ? AND z = ?"
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
