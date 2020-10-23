/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;

/**
 * @author Frostalf
 */
//Managing paramnestic's in-house tracking (Φ)
public class TrackedBlocks {

    //blockList is an empty ArrayList of type List<Block>
    private List<Block> blockList = new ArrayList<>();
    //stores TrackedBlocks if initialized.
    private static TrackedBlocks instance;
    //Constructor for TrackedBlocks
    private TrackedBlocks() {}

    //Method to check if a block is Φ.
    public boolean isTracked(Block block) {
        return this.blockList.contains(block);
    }

    //Method to get list of Φ blocks.
    public List<Block> getBlockList() {
        return this.blockList;
    }
    
    //Method to set a block as Φ.
    public boolean addToBlockList(Block block) {
        return this.blockList.add(block);
    }
    
    //Method to unset a block's Φ status.
    public boolean removeFromBlockList(Block block) {
        return this.blockList.remove(block);
    }
    
    //Initializes the class if it has not already been initialized.
    public static TrackedBlocks getInstance() {
        if (instance == null) {
            instance = new TrackedBlocks();
        }
        return instance;
    }
}