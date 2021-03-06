/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.block;


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
					//perform a restore and get a list of the actions specified
					List<String[]> blockActionListMSG = coreprotect.performRestore(
							time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, location
			    			);
			    	
			    	
			    	
			    	
			    	if(blockActionListMSG.size() == 0) {
			    		msgManager.sendMessage("No actions were found",true);
			    		return;
			    	}
			    	
			    	
			    	
			    	HashMap<ParamnesticLocation,Integer> blocks_to_be_changed = new HashMap<ParamnesticLocation,Integer>();
			    	
			    	//Go through every action
			    	for(String[] parseMSG : blockActionListMSG) {
			    		ParseResult blockAction = coreprotect.parseResult(parseMSG);
			    		int newestTime = blockAction.getTime();
			    		
			    		//Block can only be creative if blockAction.getActionId == 1 (block place)
				    	ParamnesticLocation compareKey = new ParamnesticLocation( blockAction.worldName() , blockAction.getX() , blockAction.getY() ,blockAction.getZ() , blockAction.getActionId() == 1);
				    	//Only the most recent action on every location should be selected
				    	if(blocks_to_be_changed.containsKey(compareKey))
			    			if(blocks_to_be_changed.get(compareKey) > newestTime)
			    				continue;
				    	
				    	blocks_to_be_changed.put( compareKey , newestTime );
			    	}
			    	
			    	Integer creativeBlockCounter = changeCreativeStatus(blocks_to_be_changed);
			    	
			    	long endTime = System.currentTimeMillis();
			    	
			    	msgManager.sendMessage( "Operational time: " + String.valueOf( endTime-startTime ) + "ms" , false);
			    	msgManager.sendMessage( String.valueOf(blockActionListMSG.size()) + " block actions were found, " + String.valueOf( blocks_to_be_changed.size() ) + " Blocks were set", false);
			    	msgManager.sendMessage(creativeBlockCounter.toString() + " blocks were set to creative", false);
				}
				
		},60L);
		
		return isIntercept;
	}

}
