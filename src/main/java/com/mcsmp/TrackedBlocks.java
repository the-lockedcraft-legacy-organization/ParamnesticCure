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
import static java.util.logging.Level.SEVERE;
import org.bukkit.Location;

/**
 * @author Frostalf, Thorin
 */
/**
 * Manages Paramnestic's in-house tracking (Φ)
 */
public class TrackedBlocks {

    
    private static ParamnesticCure plugin = ParamnesticCure.getInstance();
    
    /**
     * Adds the creative ID into coreprotects database
     *
     * @param location of the creative block
     */
    public static void updateCreativeIDInDB(Location location) {
    	
        plugin.getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = plugin.getCacheData().getDatabaseMap().get("coreprotect").getDatabase().getConnection();
                    
                    PreparedStatement getWorldID = connection.prepareStatement(
                    		"SELECT id FROM co_world"
                    		+ " WHERE world = ?"
                    		);
                    
                    String worldName = location.getWorld().getName();
                    getWorldID.setString(1,worldName);
                    
                    
                    ResultSet set = getWorldID.executeQuery();
                    Integer worldID = set.getInt(1);
                    
                    plugin.getLogger().info("[Manual Debug] worldID: " + String.valueOf(worldID));
                    
                    //TODO make better SQL code that doesn't repeat itself
                    // This gets the latest action on this block, action = 1 is equal to "placed a block"
                    PreparedStatement updateCreativeID = connection.prepareStatement(
                    		"UPDATE co_block"
                    		+ " SET creative = 1"
                    		+ " WHERE x = ? AND y = ? AND z = ? AND action = 1 AND wid = ?"
                    		+ " AND co_block.time IN("
                    			+ " SELECT MAX(co_block.time) FROM co_block"
                    			+ " WHERE x = ? AND y = ? AND z = ? AND action = 1 AND wid = ?"
                    		+ ")"
                    		);
                    // Don't look here
                    updateCreativeID.setInt(1, location.getBlockX());
                    updateCreativeID.setInt(2, location.getBlockY());
                    updateCreativeID.setInt(3, location.getBlockZ());
                    updateCreativeID.setInt(4, worldID);
                    // This does not exist
                    updateCreativeID.setInt(5, location.getBlockX());
                    updateCreativeID.setInt(6, location.getBlockY());
                    updateCreativeID.setInt(7, location.getBlockZ());
                    updateCreativeID.setInt(8, worldID);
                    // Don't take shrooms
                    
                    
                    
                    updateCreativeID.execute();
                    
                } catch (SQLException ex) {
                	plugin.getLogger().log(SEVERE, ex.getMessage(), ex.getCause());
                }
            }
        });
    }
    
    
}
