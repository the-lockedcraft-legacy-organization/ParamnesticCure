/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.coreprotect.CoreProtectAPI.ParseResult;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
     */
    @Override
    public void executeTask() {
        
    	ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {

			@Override
			public void run() {
				
				long startTime = System.nanoTime(); //nano Seconds
				List<String[]> blockActionListMSG = new ArrayList<String[]>();
				
				blockActionListMSG = coreprotect.performRollback(
						time,restrict_users, exclude_users, restrict_blocks, exclude_blocks,action_list, radius, radius_location
		    			);
		    	
		    	long endTime = System.nanoTime(); 
		    	
		    	msgManager.sendMessage( "Operationall time: " + String.valueOf( (endTime-startTime)*Math.pow(10, -9) ) , false);
		    	
		    	if(blockActionListMSG.size() == 0) {
		    		msgManager.sendMessage("No actions were found",true);
		    		return;
		    	}
		    	
		    	HashMap<String,Integer> blocks_to_be_changed = new HashMap<String,Integer>();
		    	Integer creativeBlockCounter = 0;
			    for(int i = blockActionListMSG.size()-1; i >= 0; i--){//cycles through the list backwards (should be slightly less costly)
			    	
			    	ParseResult blockAction = coreprotect.parseResult(blockActionListMSG.get(i));
			    	
			    	String worldname = blockAction.worldName();
			    	
			    	int oldestTime = blockAction.getTime();
			    	int x = blockAction.getX(); 	int y = blockAction.getY(); 	int z = blockAction.getZ();
			    	
			    	
			    	
			    	String compareKey = String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + worldname;
			    	
			    	
			    	if(blocks_to_be_changed.containsKey(compareKey))
		    			if(blocks_to_be_changed.get(compareKey) < oldestTime)
		    				continue;

		    		blocks_to_be_changed.put( compareKey , oldestTime );
			    	
			    	boolean isCreative = fetchDBIsCreative(oldestTime,worldname,x,y,z);
			    	
			    	if (blockAction.getActionId() == 1)
			    		isCreative = false; //When a creative block place action is rollbacked, it will get removed. An airblock must have been before this action, which is survival
			    	creativeBlockCounter += isCreative ? 1 : 0;
			    	
			    	changeCreativeStatus(x,y,z,worldname,isCreative);
			    }
			    msgManager.sendMessage( String.valueOf(blockActionListMSG.size()) + " block actions were found, " + String.valueOf( blocks_to_be_changed.size() ) + " Blocks were set", false);
			    msgManager.sendMessage(creativeBlockCounter.toString() + " blocks were set to creative", false);
			}
    		
    	},60L);
    }
}