/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

/**
 * @author InteriorCamping
 */

//Establishes plugin
public class RollbackManager {

    private static byte runCycles = 1;
    //Establishes plugin
    private ParamnesticCure plugin;
    //Establishes logger
    private Logger log = Bukkit.getLogger();
    //stores RollbackManager if initialized.
    private static RollbackManager instance;
    
    //Constructor for RollbackManager
    private RollbackManager() {
        
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
        
        while (runCycles <= 20) {
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
        }       
    }
    public static RollbackManager manageRollback() {
        if (instance == null) {
            instance = new RollbackManager();
        } else {
            runCycles = 0;
        }
        return instance;
    }
}