/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import static java.util.logging.Level.SEVERE;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.mcsmp.ParamnesticCure;
import com.mcsmp.WorldTracker;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;

/**
 * Consist of all the functions that track the location, the time, and the creative status of critical block actions
 * @author Frostalf
 * @author Thorin
 */
public class BlockTracker implements Listener {

    private static ParamnesticCure plugin = ParamnesticCure.getInstance();
    private static CoreProtectAPI coreprotect = plugin.getCoreProtect();
    
    public static long waitPeriod = plugin.getConfig().getLong("Plugin_settings.wait_time");
    /**
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	generalBlockEventTrigger(event.getBlock(),true);
    }
    /**
     *  @param event BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    	generalBlockEventTrigger(event.getBlock(),false);
    }
    /**
     * Fetches the creative id of the block, also takes care of a door bug
     * @param block the block affected
     * @param isBlockBreak
     */
    private void generalBlockEventTrigger(Block block, boolean isBlockBreak) {
    	
		final BlockState blockState = block.getState();
		
		//due to async thread limitations i have to define a variable like this
		final boolean extIsCreative = RestrictedCreativeAPI.isCreative(block);
		
		ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
			@Override
			public void run() {
				boolean isCreative = extIsCreative;
				/*
				 * On blockplace events, it is important to wait a while so that the creative logger has time to register the block
				 * whilst for blockbreak events, the status before the database updates is sought after.
				 */
				
				if(!isBlockBreak)
					isCreative = RestrictedCreativeAPI.isCreative(block);
				
				
				if(!isCreative) return;
	    		BlockTracker.updateCreativeID(block,isCreative);
	    		
	    		
	    		/*
	    		 * Makes it so that both blocks for doors get updated
	    		 */
	    		if( blockState.getType().toString().contains("DOOR") ) {
	    			Location loc = blockState.getLocation();
	    			Bisected door = (Bisected) blockState.getBlockData();
	    			if(door.getHalf() == Bisected.Half.BOTTOM)
	    				loc = loc.add(0, 1, 0);
	    			else
	    				loc = loc.add(0,-1,0);
	    			
	    			BlockTracker.updateCreativeID(loc.getBlock(),isCreative);
	    		}
			}
    	},waitPeriod);
    }
    /**
     * Checks through the coreprotect database for the most recent block place / remove action. It then calls a function that stores it (if the block fulfils the right criteria)
     *
     * @param block the block that is going to be inspected
     * @param isCreative whether the block was a creative block before the action occurred
     */
    public static void updateCreativeID(Block block,boolean isCreative) {
			
	    	
	    	int time = 0;
	    	List<String[]> actiondataMsg = coreprotect.blockLookup(block,0);
	        
	        for(String[] actionMsg : actiondataMsg) {
	        	ParseResult action = coreprotect.parseResult(actionMsg);
	        	plugin.getLogger().finer("[BlockTracker.updateCreativeID] Actionid = " + action.getActionId());
	        	if (   action.getActionId() > 2   ) continue; //has to be block place/break action
	        		
	        	time = action.getTime();
	        	
	        	break;//The returned data from parseResult seems to be ordered highest time to lowest, this should therefore return the most recent block action
	        }
	        updateCreativeID(time,block,isCreative);
    }
    /**
     * Stores the specified action if it was creative, or if there already has been a creative action already on it's location
     * @param time The time the action occurred according to coreprotect.
     * @param block
     * @param isCreative Whether the action had anything to do with a creative block
     */
    public static void updateCreativeID(int time, Block block, boolean isCreative) 
    {
    	if(time == 0) { 
    		//this should never trigger, but exists as a safety precaution
    		//ParamnesticCure relies on the loggers for block information, as it takes time for these to process, the thread will have to wait
    		plugin.getLogger().warning("An action did not get tracked properly, please increase the wait time in Plugin_settings");
    		plugin.getLogger().info("The wait time will temporarily be doubled, to avoid any further issues in this session");
			try {Thread.sleep(waitPeriod);}catch(InterruptedException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
			waitPeriod *= 2;
			updateCreativeID(block,isCreative);
    		return;
    	}
        
    	ParamnesticCure.debug("BlockTracker.updateCreativeID"," storing a action at " + block.getWorld().getName() + "," + block.getX() +" "+ block.getY() +" "+ block.getZ() + " time = " + time +  (isCreative? "creative":"survival"));
        try {
	        Connection connection = ParamnesticCure.getInstance().getConnection();
	      	PreparedStatement statement = connection.prepareStatement(
	       			"INSERT INTO blockAction (time,world,x,y,z,is_creative)"
	       			+ " VALUES (?,?,?,?,?,?);"
	       			);
	            	
	      	statement.setInt( 1, time);
	      	statement.setInt( 2, WorldTracker.getWorldId( block.getWorld().getName()) );
	      	statement.setInt( 3, block.getX());
	      	statement.setInt( 4, block.getY());
	      	statement.setInt( 5, block.getZ());
	      	statement.setInt( 6,  isCreative ? 1 : 0  );

	       	
	      	statement.execute();
	      	statement.close();
	       	connection.close();
        }catch(SQLException ex) {
	       	ParamnesticCure.debug("BlockTracker.updateCreativeID"," Action has already been stored" );
	       	}
            
    }
    
    /**
     * Purges all data from the database older than specified time
     * @param time
     */
    public static void purgeDatabase(int time) {
    	
    	int oldestDataPoint = Math.round(System.currentTimeMillis()/1000) - time;//seconds
    	try {
    		Connection connection = ParamnesticCure.getInstance().getConnection();
 	    	PreparedStatement statement = connection.prepareStatement(
 	      			"DELETE FROM blockAction"
 	      			+ " WHERE time < ?"
 	      			);
 	            	
 	    	statement.setInt( 1, oldestDataPoint);
 	       	
 	    	statement.execute();
 	    	statement.close();
 	       	connection.close();
 	       	
 	       plugin.getLogger().info("[Purge] Succesfully purged the ParamnesticCure database");
        }catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
             
    }
}