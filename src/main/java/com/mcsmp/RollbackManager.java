/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;
import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author InteriorCamping
 */

//Establishes plugin
public class RollbackManager {

    private static byte runCycles = 1;
    //Establishes plugin
    private ParamnesticCure plugin;
    //Establishes logger
    private Logger log = getLogger();
    //stores RollbackManager if initialized.
    private static RollbackManager instance;

    //Constructor for RollbackManager
    public RollbackManager() {
        HashMap<Integer, CoreProtectData> activeSearch = new HashMap<>();
        HashMap<Integer, String> worldData = new HashMap<>();
        String dbGMC = plugin.getConfig().getString("databases.logger.database");
        //TODO
        /*_____ ___  ___   ___
         |_   _/ _ \|   \ / _ \
           | || (_) | |) | (_) |
           |_| \___/|___/ \___/
         -= Database Query One =-

        Query dbGMC
        Put the values of co_world into worldData
        */
        double activeX;
        double activeY;
        double activeZ;
        boolean activeAction;



        //while (runCycles <= 20) {
            //TODO
            /*_____ ___  ___   ___
             |_   _/ _ \|   \ / _ \
               | || (_) | |) | (_) |
               |_| \___/|___/ \___/
             -= Database Query Two =-

            log_db = "databases.logger.database"
            log_tb = "databases.logger.table"
            log_wd = "databases.logger.world_table"

            SELECT

            ALL ROWS in log_db/log_tb
            w/ time >= Instant.now().getEpochSecond() - 10s
            & rolled_back = 1

            For each row selected,
            transform into Action (Boolean) and Location (Location)
            using the following columns of co_block

            wid = String -->> use worldData to convert to world for location
            x = int
            y = int
            z = int
            action = boolean (represented as 1[place] & 0[break])


            and store in activeSearch
            */

            /*
             for each Location,
               if Action is 0
                  Check if isTracked()
                     Use API to add to Creative
                        then removeFromBlockList()
               else if action is 1
                  Use API to check creative
                     if creative
                        addToBlockList()
                        use API to remove creative
               else
                  severe error.
            */

            /* if activeSearch was empty
               byte runCycles++
            */
        //}
    }

    public void executeTask() {
        ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Connection connection = ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("creative").getConnection();
                    //co_world
                    PreparedStatement statement = connection.prepareStatement("SELECT * from co_block,co_world INNER JOIN co_world ON co_block.wid=co_world.id");
                    ResultSet set = statement.executeQuery();
                    while(set.next()) {
                        int action = set.getInt("action");
                        Location location = new Location(ParamnesticCure.getInstance().getServer().getWorld(set.getString("world")), set.getInt("x"), set.getInt("y"), set.getInt("z"));
                        if(set.getInt("rollback") > 0) {
                            switch(action) {
                                case 0:
                                    if(ParamnesticCure.getInstance().getTrackedBlocks().getBlockList().containsKey(location)) {
                                        ParamnesticCure.getInstance().getTrackedBlocks().removeFromBlockList(location);
                                        RestrictedCreativeAPI.add(location.getBlock());
                                    }
                                    break;
                                case 1:
                                        ParamnesticCure.getInstance().getTrackedBlocks().addToBlockList(location);
                                        RestrictedCreativeAPI.remove(location.getBlock());
                                    break;
                                default: break;
                            }
                        } else {
                            switch(action) {
                                case 0:
                                        ParamnesticCure.getInstance().getTrackedBlocks().addToBlockList(location);
                                        RestrictedCreativeAPI.remove(location.getBlock());
                                    break;
                                case 1:
                                    if(ParamnesticCure.getInstance().getTrackedBlocks().getBlockList().containsKey(location)) {
                                        ParamnesticCure.getInstance().getTrackedBlocks().removeFromBlockList(location);
                                        if(!(location.getBlock().isEmpty() || location.getBlock().isLiquid())) {
                                            RestrictedCreativeAPI.add(location.getBlock());
                                        }
                                    }
                                    break;
                                default: break;
                            }
                        }
                    }
                } catch (SQLException ex) {

                }
            }
        }, 1200L);
    }
}