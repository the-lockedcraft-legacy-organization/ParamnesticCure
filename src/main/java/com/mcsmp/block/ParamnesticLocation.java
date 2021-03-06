package com.mcsmp.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.mcsmp.ParamnesticCure;

import me.prunt.restrictedcreative.RestrictedCreativeAPI;

/**
 * This is mainly a class of convenience, just adds a creative status to a location (which can later be used handily in a hashmap)
 * @author Thorin
 */
public class ParamnesticLocation extends Location{
	//This is needed for the current rollback / restore algorithm to work properly:
	// - When a block place action gets rollbacked it is always going to rollback to a survival block (air)
	// - When a block break action gets restored it is always going to restore to a survival block (air)
	private boolean canBeCreative = false; 
	/**
	 * 
	 * @param worldname
	 * @param x
	 * @param y
	 * @param z
	 * @param canBeCreative If this location can have a creative status
	 */
	public ParamnesticLocation(String worldname, int x, int y, int z,boolean canBeCreative) {
		super(Bukkit.getWorld(worldname),x,y,z);
		this.canBeCreative = canBeCreative;
	}
	/**
	 * As i don't care if the canBeCreative status is the same for the to objects in an equals statement, I will redefine it
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
	    if (!(obj instanceof ParamnesticLocation)) {
	        return false;
	    } else {
	    	ParamnesticLocation that = (ParamnesticLocation)obj;
	        return super.equals(that.clone());
	    }
	}
	/**
	 * As I redefined the equals method, I have to change this function. The equals and hashCode method are closely interlinked.
	 */
	@Override
	public int hashCode() {
	    return super.hashCode();
	}
	
	
	/**
	 * Changes the creative status in restrictedcreative, but also internally
	 * @param isCreative
	 */
	public int setCreativeStatus(boolean isCreative) {
		isCreative = canBeCreative && isCreative;
		ParamnesticCure.debug("ParamnesticLocation.setCreativeStatus","Setting creative status as " + (isCreative?"creative":"survival"));
		
		Block block = getBlock();
		if(isCreative) {
			RestrictedCreativeAPI.add(block);
		}
		else {
			RestrictedCreativeAPI.remove(block);
		}
		return isCreative?1:0;
	}
}