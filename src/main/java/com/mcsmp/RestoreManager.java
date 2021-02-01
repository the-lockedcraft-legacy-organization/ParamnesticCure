package com.mcsmp;

import java.util.List;

import org.bukkit.Location;
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
					
					List<String[]> affectedBlocksMsg = coreprotect.performRestore(
							time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, radius_location
			    			);
			    	
			    	long endTime = System.nanoTime();
			    	ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Operationall time: " + String.valueOf(  endTime-startTime  ) + " ns");
			    	
			    	
			    	
			    	
				}
			
		},60L);
	}

}
