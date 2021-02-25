/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.loggers;


import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.mcsmp.MessageManager;
import com.mcsmp.ParamnesticCure;

import net.coreprotect.CoreProtectAPI.ParseResult;
/**
 * Does a series of logical operations to minimise opportunities for restores to mess with creative block data.
 * Note that this does not include restore interference with inventories!
 * 
 * @author Thorin
 */
public class RestoreManager extends LoggerManager {
	/**
     * Constructor for RestoreManager
     * @param arguments : The arguments of the command
     * @param location : location where command was thrown
     */
	public RestoreManager(String[] arguments, Location location,Player player) {
		
		this.msgManager = new MessageManager(player,"Restore");
		this.coreprotect = ParamnesticCure.getInstance().getCoreProtect();
    	
    	interpretArguments(arguments,location);
	}
	
	/**
	 * This runs all the logic that is needed when performing a restore:
	 * 
	 * Calls the restore function from blocklogger API and gets all actions that are affected
	 * 
	 * It then checks through all the returned values, and selects the most recent action on every location,
	 * To then call the changeCreativeStatus function on that action
	 * @return true if this should cancel the event
	 */
	@Override
	public boolean executeTask() {
		if(isCancelled)
			return isIntercept;
		ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {

				@Override
				public void run() {
					long startTime = System.currentTimeMillis();
					
					List<String[]> blockActionListMSG = coreprotect.performRestore(
							time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, location
			    			);
			    	
			    	
			    	
			    	
			    	if(blockActionListMSG.size() == 0) {
			    		msgManager.sendMessage("No actions were found",true);
			    		return;
			    	}
			    	
			    	
			    	
			    	HashMap<String,Integer> blocks_to_be_changed = new HashMap<String,Integer>();
			    	Integer creativeBlockCounter = 0;
			    	
			    	for(String[] parseMSG : blockActionListMSG) {
			    		ParseResult blockAction = coreprotect.parseResult(parseMSG);
			    		
			    		int x = blockAction.getX(); 	int y = blockAction.getY(); 	int z = blockAction.getZ();
			    		int newestTime = blockAction.getTime(); String worldname = blockAction.worldName();
				    	
				    	String compareKey = String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + worldname;
			    		if(blocks_to_be_changed.containsKey(compareKey))
			    			if(blocks_to_be_changed.get(compareKey) > newestTime)
			    				continue;
			    		
			    		
			    		
			    		blocks_to_be_changed.put( compareKey , newestTime );
			    		
			    		boolean isCreative = fetchDBIsCreative(newestTime,worldname,x,y,z);
			    		
			    		if(blockAction.getActionId() == 0) //Any blockbreak action will result in a survival block (air block)
			    			isCreative = false;
			    		
				    	creativeBlockCounter += isCreative? 1 : 0;
				    	changeCreativeStatus(x,y,z,worldname,isCreative);
			    	}
			    	
			    	long endTime = System.currentTimeMillis();
			    	
			    	msgManager.sendMessage( "Operational time: " + String.valueOf( endTime-startTime ) + "ms" , false);
			    	msgManager.sendMessage( String.valueOf(blockActionListMSG.size()) + " block actions were found, " + String.valueOf( blocks_to_be_changed.size() ) + " Blocks were set", false);
			    	msgManager.sendMessage(creativeBlockCounter.toString() + " blocks were set to creative", false);
				}
				
		},60L);
		
		return isIntercept;
	}

}
