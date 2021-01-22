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
import me.prunt.restrictedcreative.RestrictedCreativeAPI;
import org.bukkit.Location;

/**
 * @author InteriorCamping
 */

/**
 * Does a series of logical operations to minimize opportunities for rollblacks to mess with creative data.
 * Note that this does not include rollback interference with inventories!
 */
public class RollbackManager {

    /*
     * Constructor for Rollbacks.
     */
    public RollbackManager(String command) {
    	
    }

    // ┌─────────────────────────────────────────── /!\=- 𝗪𝗔𝗥𝗡𝗜𝗡𝗚 /!\ ─────────────────────────────────────────
    // │
    // │  𝘛𝘩𝘪𝘴 𝘪𝘴 𝘢 𝘩𝘪𝘨𝘩𝘭𝘺 𝘤𝘰𝘮𝘱𝘭𝘦𝘹 𝘰𝘱𝘦𝘳𝘢𝘵𝘪𝘰𝘯! 𝘐𝘵 𝘤𝘰𝘯𝘴𝘪𝘥𝘦𝘳𝘴 𝘵𝘩𝘳𝘦𝘦 𝘥𝘦𝘨𝘳𝘦𝘦𝘴 𝘰𝘧 𝘣𝘭𝘰𝘤𝘬 𝘰𝘱𝘦𝘳𝘢𝘵𝘪𝘰𝘯𝘴 (𝘵𝘸𝘦𝘭𝘷𝘦 𝘴𝘵𝘢𝘵𝘦𝘴 𝘪𝘯 𝘵𝘰𝘵𝘢𝘭!)
    // │ 𝘖𝘯𝘭𝘺 𝘵𝘰𝘶𝘤𝘩 𝘵𝘩𝘪𝘴 𝘭𝘰𝘨𝘪𝘤 𝘪𝘧 𝘺𝘰𝘶 𝘩𝘢𝘷𝘦 𝘢 𝘴𝘵𝘳𝘰𝘯𝘨 𝘶𝘯𝘥𝘦𝘳𝘴𝘵𝘢𝘯𝘥𝘪𝘯𝘨 𝘰𝘧 𝘱𝘦𝘳𝘮𝘶𝘵𝘢𝘵𝘪𝘰𝘯𝘴 𝘢𝘯𝘥 𝘮𝘢𝘯𝘺 𝘩𝘰𝘶𝘳𝘴 𝘵𝘰 𝘵𝘦𝘴𝘵 𝘺𝘰𝘶𝘳 𝘤𝘩𝘢𝘯𝘨𝘦𝘴!
    // │
    // │           𝗘𝘃𝗲𝗻 𝗼𝗻𝗲 𝘀𝗺𝗮𝗹𝗹 𝗰𝗵𝗮𝗻𝗴𝗲 𝘁𝗼 𝘁𝗵𝗶𝘀 𝘀𝗲𝗰𝘁𝗶𝗼𝗻 𝗶𝘀 𝗲𝗻𝗼𝘂𝗴𝗵 𝘁𝗼 𝗺𝗲𝘀𝘀 𝘁𝗵𝗲 𝘄𝗵𝗼𝗹𝗲 𝘁𝗵𝗶𝗻𝗴 𝘂𝗽!
    // │
    // └──────────────────────────────────────────────────────────────────────────────────────────────────────────
    /*
     * Performs a series of logical operations to determine if the blocks getting rolled back should be protected by creative mode.
     */
    public void executeTask() {
        ParamnesticCure.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(ParamnesticCure.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = ParamnesticCure.getInstance().getCacheData().getDatabaseMap().get("coreprotect").getDatabase().getConnection();
                    //co_world
                    PreparedStatement statement = connection.prepareStatement(
                    		"SELECT * from co_block,co_world"
                    		+ " INNER JOIN co_world"
                    		+ " ON co_block.wid=co_world.id");
                    
                    ResultSet set = statement.executeQuery();
                    while(set.next()){
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
                        } 
                        else {
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
        }, 60L);
    }
}