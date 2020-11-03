/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Frostalf
 */
//Managing paramnestic's in-house tracking (Φ)
public class TrackedBlocks {

    //TODO convert TrackedBlocks to use the ParamnesticCure database.
    //blockList is an empty ArrayList of type List<Block>
    private ConcurrentHashMap<Location, Integer> blockList = new ConcurrentHashMap<>();
    //stores TrackedBlocks if initialized.
    private static TrackedBlocks instance;
    public static int id = 0;

    //Constructor for TrackedBlocks
    private TrackedBlocks() {
        loadBlocks();
    }

    //Method to check if a block is Φ.
    public boolean isTracked(Location location) {
        return this.blockList.containsKey(location);
    }

    //Method to get list of Φ blocks.
    public ConcurrentHashMap<Location, Integer> getBlockList() {
        return this.blockList;
    }

    //Method to set a block as Φ.
    /**
     *
     * @param location
     * @return returns true if successful, false otherwise.
     */
    public boolean addToBlockList(Location location) {
        return this.blockList.putIfAbsent(location, addToDB(location)) != null;
    }

    //Method to unset a block's Φ status.
    public void removeFromBlockList(Location location) {
        this.blockList.remove(location);
    }

    private void loadBlocks() {
        ParamnesticCure.getInstance().getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    ResultSet set = ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("paramnestic").getResults("Select * FROM blocks");
                    while (set.next()) {
                        Location location = new Location(ParamnesticCure.getInstance().getServer().getWorld(set.getString("world")), set.getInt("x"), set.getInt("y"), set.getInt("z"));
                        getBlockList().put(location, set.getInt("id"));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TrackedBlocks.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private Integer addToDB(Location location) {
        ParamnesticCure.getInstance().getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("paramnesticcure").getConnection().prepareStatement("INSERT INTO blocks (world, x, y , z) VALUES(?,?,?,?)");
                    statement.setString(1, location.getWorld().toString());
                    statement.setDouble(2, location.getBlockX());
                    statement.setDouble(3, location.getBlockY());
                    statement.setDouble(4, location.getBlockZ());
                    id = statement.executeQuery().getInt("id");
                } catch (SQLException ex) {

                }
            }
        });
        return id;
    }

    public void save() {
        ParamnesticCure.getInstance().getServer().getScheduler().runTaskAsynchronously(ParamnesticCure.getInstance(), new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("paramnesticcure").getConnection().prepareStatement("UPDATE blocks SET world = ?, SET x = ?, SET y = ?, SET z = ?, WHERE id = ?");
                    for (Location location : blockList.keySet()) {
                        statement.setString(1, location.getWorld().toString());
                        statement.setDouble(2, location.getBlockX());
                        statement.setDouble(3, location.getBlockY());
                        statement.setDouble(4, location.getBlockZ());
                        statement.setInt(5, blockList.get(location));
                        statement.executeUpdate();
                        ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("paramnesticcure").getConnection().commit();
                    }
                } catch (SQLException ex) {
                    getLogger(TrackedBlocks.class.getName()).log(SEVERE, null, ex);
                }
            }
        });

    }

    //Initializes the class if it has not already been initialized.
    public static TrackedBlocks getInstance() {
        if (instance == null) {
            instance = new TrackedBlocks();
        }
        return instance;
    }
}