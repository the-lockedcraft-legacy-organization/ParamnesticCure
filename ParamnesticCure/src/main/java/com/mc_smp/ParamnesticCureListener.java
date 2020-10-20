/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * @author Frostalf
 */
public class ParamnesticCureListener implements Listener {

    private ParamnesticCure plugin;

    public ParamnesticCureListener(ParamnesticCure plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockState blockState = block.getState();

        if(blockState.hasMetadata("GMC")) {
            plugin.getTrackedBlocks().addToBlockList(block);
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if(player.getGameMode() == GameMode.CREATIVE) {

        }
    }

    @EventHandler
    public void commandRollBack(PlayerCommandPreprocessEvent event) {
        if(event.getMessage().contains("/rollback")) {

        }
    }

    @EventHandler
    public void serverCommandRollBack(ServerCommandEvent event) {
        if(event.getCommand().equalsIgnoreCase("rollback")) {

        }
    }
}
