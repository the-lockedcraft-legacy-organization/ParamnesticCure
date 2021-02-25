/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.loggers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
    public static long waitPeriod = 50L;
    /**
     * Only tracking block break events, as those are the only necessary event that needs to get tracked; All critical actions are blockbreak events (except on rollback rollback)
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	generalBlockEventTrigger(event.getBlock(),true);
    }
    /**
     * Currently not in use. It would be unnecessary to track blockplace event's as blocks can be creative even though no blockplace event occured (for example a piston moving a creative block)
     * @param event BlockPlaceEvent
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
    	},BlockTracker.waitPeriod);
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
    	
    	/*
    	 * Avoids duplicate entries, but also irrelevant blocks
    	 */
    	int IsInDB = isInDatabase(block,time);
    	
    	
    	if(   !isCreative && ( IsInDB == 0)  || IsInDB == 2 ) return;
    	
    	
    	if(time == 0) { 
    		//this should never trigger, but exists as a safety precaution
    		//ParamnesticCure relies on the loggers for block information, as it takes time for these to process, the thread will have to wait
    		plugin.getLogger().warning("A action didn't get tracked properly. Please contact plugin author");
			try {Thread.sleep(waitPeriod);}catch(InterruptedException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
			waitPeriod *= 2;
			updateCreativeID(block,isCreative);
    		return;
    	}
        
    	plugin.getLogger().finer("[BlockTracker.updateCreativeID] storing a block as" + (isCreative? "creative":"survival"));
        try {
	        Connection connection = ParamnesticCure.getInstance().getConnection();
	      	PreparedStatement addToDatabase = connection.prepareStatement(
	       			"INSERT INTO blockAction (time,world,x,y,z,is_creative)"
	       			+ " VALUES (?,?,?,?,?,?);"
	       			);
	            	
	       	addToDatabase.setInt( 1, time);
	       	addToDatabase.setInt( 2, WorldTracker.getWorldId( block.getWorld().getName() ));
	       	addToDatabase.setInt( 3, block.getX());
	       	addToDatabase.setInt( 4, block.getY());
	       	addToDatabase.setInt( 5, block.getZ());
	       	addToDatabase.setInt( 6,  isCreative ? 1 : 0  );

	       	
	       	addToDatabase.execute();
	       	
	       	
	       	//plugin.getLogger().info("[Manual Debug] Added the block action into the database as " + (isCreative ? "creative" : "survival") );
        }catch(SQLException ex) {}
            
    }
    /**
     * 
     * @param block
     * @param time the time of an action
     * 
     * @return Is this block in the database?
     *  0 => No.   
     *  1 => yes, but not this action.   
     *  2 => yes, this action is already stored.
     */
    public static int isInDatabase(Block block, int time) {

    	try {
	    	Connection connection = ParamnesticCure.getInstance().getConnection();
	        PreparedStatement getCreativeStatus = connection.prepareStatement(
	        		"SELECT time FROM blockAction INNER JOIN worlds"
	                + " ON blockAction.world = worlds.world_id"
	        		+ " WHERE worlds.world = ? AND x = ? AND y = ? AND z = ?"
	        		);
	        getCreativeStatus.setString(  1, block.getWorld().getName() );
	        getCreativeStatus.setInt(  2, block.getX()  );
	        getCreativeStatus.setInt(  3, block.getY()  );
	        getCreativeStatus.setInt(  4, block.getZ()  );
	        
	        ResultSet set = getCreativeStatus.executeQuery();
	        
	        
	        if(set.next()) {
		        do{
		        	if( set.getInt(1) == time ) // An action at the same timeperiod exist
		        		return 2;
	        	}while ( set.next() );
		        
		        return 1; // No replica action was found
	        }
    	}
    	catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	return 0;
    }
    
    /**
     * Purges all data from the database older than specified time
     * @param time
     */
    public static void purgeDatabase(int time) {
    	
    	int oldestDataPoint = Math.round(System.currentTimeMillis()/1000) - time;//seconds
    	try {
    		Connection connection = ParamnesticCure.getInstance().getConnection();
 	    	PreparedStatement addToDatabase = connection.prepareStatement(
 	      			"DELETE FROM blockAction"
 	      			+ " WHERE time < ?"
 	      			);
 	            	
 	    	addToDatabase.setInt( 1, oldestDataPoint);
 	       	
 	    	addToDatabase.execute();
 	       	
 	       	
 	       plugin.getLogger().info("[Purge] Succesfully purged the ParamnesticCure database");
        }catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
             
    }
}