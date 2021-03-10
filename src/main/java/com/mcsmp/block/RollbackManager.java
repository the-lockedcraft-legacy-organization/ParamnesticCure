/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.block;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.coreprotect.CoreProtectAPI.ParseResult;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.mcsmp.MessageManager;
import com.mcsmp.ParamnesticCure;

/**
 * Does a series of logical operations to minimise opportunities for rollblacks to mess with creative block data.
 * Note that this does not include rollback interference with inventories!
 * 
 * @author InteriorCamping
 * @author Thorin
 */
public class RollbackManager extends LoggerManager{
	
	/**
     * Constructor for RollbackManager
     * @param arguments : The arguments of the command
     * @param radius_location : location where command was thrown
     */
    public RollbackManager(String[] arguments, Location radius_location, Player player) {
    	
    	this.msgManager = new MessageManager(player,"Rollback");
    	
    	this.coreprotect = ParamnesticCure.getInstance().getCoreProtect();
    	
    	interpretArguments(arguments,radius_location);
    }

    /**
     * This runs all the logic that is needed when performing a rollback:
	 * 
	 * Calls the rollback function from blocklogger API and gets all actions that are affected
	 * 
	 * It then checks through all the returned values, and selects the oldest action on every location,
	 * To then call the changeCreativeStatus function on that action
	 * @return true if this should cancel the event
     */
    @Override
    public boolean executeTask() {
    	if(isCancelled)
			return isIntercept;
    	ParamnesticCure.getInstance().getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new Runnable() {

			@Override
			public void run() {
				
				long startTime = System.currentTimeMillis(); //nano Seconds
				List<String[]> blockActionListMSG = new ArrayList<String[]>();
				//perform a rollback and get a list of the actions specified
				blockActionListMSG = coreprotect.performRollback(
						time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, location
		    			);
		    	
		    	
		    	
		    	
		    	
		    	if(blockActionListMSG.size() == 0) {
		    		msgManager.sendMessage("No actions were found",true);
		    		return;
		    	}
		    	
		    	HashMap<ParamnesticLocation,Integer> blocks_to_be_changed = new HashMap<ParamnesticLocation,Integer>();
		    	
		    	
		    	//Go through every action
			    for(int i = blockActionListMSG.size()-1; i >= 0; i--){//cycles through the list backwards (should be slightly less costly)
			    	
			    	ParseResult blockAction = coreprotect.parseResult(blockActionListMSG.get(i));
			    	
			    	int oldestTime = blockAction.getTime();
			    	
			    	
			    	
			    	//Block can only be creative if blockAction.getActionId == 0 (block break)
			    	ParamnesticLocation compareKey = new ParamnesticLocation( blockAction.worldName() , blockAction.getX() , blockAction.getY() ,blockAction.getZ() , blockAction.getActionId() == 0);
			    	//Only the oldest action on every location should be selected
			    	if(blocks_to_be_changed.containsKey(compareKey))
		    			if(blocks_to_be_changed.get(compareKey) < oldestTime)
		    				continue;
			    	
			    	blocks_to_be_changed.put( compareKey , oldestTime );
			    }
			    
			    
			    Integer creativeBlockCounter = changeCreativeStatus(blocks_to_be_changed);
			    
			    long endTime = System.currentTimeMillis(); 
		    	msgManager.sendMessage( "Operational time: " + String.valueOf( endTime-startTime ) + "ms", false);
			    msgManager.sendMessage(
			    		String.valueOf(blockActionListMSG.size()) + " block "+ "action"+ (blockActionListMSG.size() == 1?"":"s") +" were found, " + String.valueOf( blocks_to_be_changed.size() ) + " Block"+ (blocks_to_be_changed.size() == 1?"":"s") + " were set"
			    		, false);
			    msgManager.sendMessage(creativeBlockCounter.toString() + " Block"+ (creativeBlockCounter == 1?"":"s")+" were set to creative", false);
			}
    		
    	});
    	
    	return isIntercept;
    }
}