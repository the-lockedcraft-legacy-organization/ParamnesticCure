/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;


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
			    	
			    	
			    	
			    	HashMap<String,Integer> blocks_to_be_changed = new HashMap<String,Integer>();
			    	Integer creativeBlockCounter = 0;
			    	
			    	for(String[] parseMSG : blockActionListMSG) {
			    		ParseResult blockAction = coreprotect.parseResult(parseMSG);
			    		
			    		if(!blockAction.isRolledBack())	continue;

			    		int x = blockAction.getX(); 	int y = blockAction.getY(); 	int z = blockAction.getZ();
			    		int newestTime = blockAction.getTime(); String worldname = blockAction.worldName();
				    	
				    	String compareKey = String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + worldname;
			    		if(blocks_to_be_changed.containsKey(compareKey))
			    			if(blocks_to_be_changed.get(compareKey) > newestTime)
			    				continue;
			    		
			    		
			    		
			    		blocks_to_be_changed.put( compareKey , newestTime );
			    		
			    		int isCreative = fetchDBIsCreative(newestTime,worldname,x,y,z);
			    		
			    		if(blockAction.getActionId() == 0) //Any blockbreak action will result in a survival block (air block)
			    			isCreative = 0;
			    		

				    	creativeBlockCounter += isCreative;
				    	changeCreativeStatus(x,y,z,worldname,isCreative);
			    	}
			    	
			    	msgManager.sendMessage( String.valueOf(blockActionListMSG.size()) + " block actions were found, " + String.valueOf( blocks_to_be_changed.size() ) + " Blocks were set", false);
			    	msgManager.sendMessage(creativeBlockCounter.toString() + " blocks were set to creative", false);
				}
			
				
				
		},60L);
	}

}
