/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Frostalf
 */
/**
 * Manages Paramnestic's in-house tracking (Φ)
 */
public class TrackedBlocks {

    //Creates a hashmap for Φ blocks
    private ConcurrentHashMap<Location, Integer> blockList = new ConcurrentHashMap<>();
    private static ParamnesticCure plugin = ParamnesticCure.getInstance();
    //stores TrackedBlocks if initialized.
    private static TrackedBlocks instance;

    //Constructor for TrackedBlocks

    /**
     * Manages adding blocks recently marked as Φ to Paramnestic's database.
     *
     * @param location a Location to set as Φ
     * @return returns true if successful, false otherwise.
     */
    public void updateCreativeIDInDB(Location location) {
    	
    	Integer time = Math.round( System.currentTimeMillis()/1000);//Seconds
    	
        plugin.getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = plugin.getCacheData().getDatabaseMap().get("coreprotect").getDatabase().getConnection();


                    PreparedStatement getWorldID = connection.prepareStatement(
                    		"SELECT * co_world"
                    		+ " WHERE world = ?"
                    		);
                    getWorldID.setString(1,location.getWorld().toString());
                    
                    
                    Integer worldID = getWorldID.executeQuery().getInt(1);
                    
                    
                    
                    //TODO make better SQL code that doesn't repeat itself
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
                    
                    
                    
                    updateCreativeID.executeQuery();
                    
                } catch (SQLException ex) {
                	plugin.getLogger().log(SEVERE, ex.getMessage(), ex.getCause());
                }
            }
        });
    }

    /**
     * Integrates recent changes to Φ into Paramnestic's database.
     */
    //no real need for this
    public void save() {
        try {
            File file = new File(plugin.getDataFolder().getAbsolutePath(), "paramnestic.db");
            String url = ("jdbc:sqlite:" + file.getAbsoluteFile());
            Connection connection = DriverManager.getConnection(url);
            PreparedStatement statement = connection.prepareStatement(
            		"UPDATE blocks"
            		+ " SET world = ?, x = ?, y = ?, z = ?"
            		+ " WHERE id = ?");
            for (Location location : blockList.keySet()) {
                statement.setString(1, location.getWorld().toString());
                statement.setDouble(2, location.getBlockX());
                statement.setDouble(3, location.getBlockY());
                statement.setDouble(4, location.getBlockZ());
                statement.setInt(5, blockList.get(location));
                statement.executeUpdate();
                connection.commit();
            }
        } catch (SQLException ex) {
            getLogger(TrackedBlocks.class.getName()).log(SEVERE, null, ex);
        }
    }

    /**
     * Initializes the TrackedBlocks class if it has not already been
     * initialized.
     *
     * @return returns true if successful, false otherwise.
     */
    public static TrackedBlocks getInstance() {
        if (instance == null) {
            instance = new TrackedBlocks();
        }
        return instance;
    }
}
