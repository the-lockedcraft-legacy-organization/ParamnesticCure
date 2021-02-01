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
import org.bukkit.Location;
import org.bukkit.block.Block;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;
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
    /**
     * Adds a critical block action into coreprotects database, 
     *
     * @param block the block that is going to inspected
     */
    public static void updateCreativeIDInDB(Block block) {
    	boolean isCreative = RestrictedCreativeAPI.isCreative(block);

    	

        plugin.getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
            @Override
            public void run() {
            	List<String[]> actiondataMsg = coreprotect.blockLookup(block,0);
            	
            	String player = "";
            	int time = 0;
            	
            	for(String[] actionMsg : actiondataMsg) {
            		ParseResult action = coreprotect.parseResult(actionMsg);
            		if (action.getActionId() != 1) continue; //has to be block place action : actionId == 1
            		
            		
            		player = action.getPlayer();
            		time = action.getTime();
            		break;//The returned data from parsResult seems to be ordered highest time to lowest, this should return the most recent block place action
            	}
            	// Any block that has had an creative action is deemed as a critical block; all actions needs to be logged
            	if(!isCreative && (isInDatabase(block,time) != 1)) return;
            	
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

            	}catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
            }
        });
    }
    /**
     * 
     * @param block
     * @param time
     * @return 0 => No, 1 => yes, but not this action, 2 => yes, the action is already stored
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
		        	if( set.getInt(1) == time )
		        		return 2;
	        	}while ( set.next() );
		        
		        return 1;
	        }
    	}
    	catch(SQLException ex) {ParamnesticCure.getInstance().getLogger().log(SEVERE, ex.getMessage(), ex.getCause());}
    	return 0;
    }
}