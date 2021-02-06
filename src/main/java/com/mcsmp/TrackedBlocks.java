/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import static java.util.logging.Level.SEVERE;
import org.bukkit.block.Block;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;

/**
 * Manages Paramnestic's in-house tracking (Φ)
 * @author Frostalf
 * @author Thorin
 */
public class TrackedBlocks {

    private static ParamnesticCure plugin = ParamnesticCure.getInstance();
    private static CoreProtectAPI coreprotect = plugin.getCoreProtect();
    
    public static long waitPeriod = 50L;
    /**
     * Adds a critical block action into coreprotects database, 
     *
     * @param block the block that is going to be inspected
     */
    public static void updateCreativeID(Block block,boolean isCreative) {
			
	    	
	    	String player = "";
	    	int time = 0;
	    	List<String[]> actiondataMsg = coreprotect.blockLookup(block,0);
	        
	    	
	        for(String[] actionMsg : actiondataMsg) {
	        	ParseResult action = coreprotect.parseResult(actionMsg);
	        	plugin.getLogger().info("[Manual Debug] Tracked blocks: Looking at block: x=" + action.getX() + ", y=" + action.getY() + ", z=" + action.getZ()+ ", time = " + action.getTime() + ", Id = " + action.getActionString());
	        	if (action.getActionId() != 1) continue; //has to be block place action : actionId == 1
	        		
	        		
	        	player = action.getPlayer();
	        	time = action.getTime();
	        	
	        	break;//The returned data from parseResult seems to be ordered highest time to lowest, this should therefore return the most recent block place action
	        }
	        
	    	updateCreativeID(time,player,block,isCreative);
			
    }
    
    public static void updateCreativeID(int time, String player, Block block, boolean isCreative) 
    {
    	/**
    	 * Avoids duplicate entries, but also irrelevant blocks
    	 */
    	if( (  !isCreative && (isInDatabase(block,time) == 0)  ) || isInDatabase(block,time) == 2) return;
    	
    	
    	if(time == 0) { //this should never trigger, but as a safety precaution
    		//ParamnesticCure relies on the loggers for block information, as it takes time for these to process, the thread will have to wait
			try {Thread.sleep(waitPeriod);}catch(InterruptedException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
			waitPeriod *= 2;
			updateCreativeID(block,isCreative);
    		return;
    	}
        
    	
        try {
	        Connection connection = ParamnesticCure.getInstance().getConnection();
	      	PreparedStatement addToDatabase = connection.prepareStatement(
	       			"INSERT INTO blockAction (time,user,world,x,y,z,is_creative)"
	       			+ " VALUES (?,?,?,?,?,?,?)"
	       			);
	            	
	       	addToDatabase.setInt( 1, time);
	       	addToDatabase.setString( 2, player);
	       	addToDatabase.setString( 3, block.getWorld().getName());
	       	addToDatabase.setInt( 4, block.getX());
	       	addToDatabase.setInt( 5, block.getY());
	       	addToDatabase.setInt( 6, block.getZ());
	       	addToDatabase.setInt( 7,  isCreative ? 1 : 0  );

	       	
	       	addToDatabase.execute();
	       	
	       	
	       	plugin.getLogger().info("[Manual Debug] Added the block action into the database");
        }catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
            
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
	        		"SELECT time FROM blockAction"
	        		+ " WHERE world = ? AND x = ? AND y = ? AND z = ?"
	        		);
	        getCreativeStatus.setString(  1, block.getWorld().getName()  );
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
    
    public static void purgeDatabase(int time, String world) {
    	
    }
}