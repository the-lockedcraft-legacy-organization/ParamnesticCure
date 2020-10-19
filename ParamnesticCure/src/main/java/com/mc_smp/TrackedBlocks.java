/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mc_smp;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;

/**
 *
 * @author Frostalf
 */
public class TrackedBlocks {

    private List<Block> blockList = new ArrayList<>();
    private static TrackedBlocks instance;

    private TrackedBlocks() {}

    public boolean isTracked(Block block) {
        return this.blockList.contains(block);
    }

    public List<Block> getBlockList() {
        return this.blockList;
    }

    public boolean addToBlockList(Block block) {
        return this.blockList.add(block);
    }

    public boolean removeFromBlockList(Block block) {
        return this.blockList.remove(block);
    }

    public static TrackedBlocks getInstance() {
        if (instance == null) {
            instance = new TrackedBlocks();
        }
        return instance;
    }
}
