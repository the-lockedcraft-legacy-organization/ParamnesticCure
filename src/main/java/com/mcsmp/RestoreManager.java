package com.mcsmp;

import org.bukkit.Location;
/**
 * Does a series of logical operations to minimise opportunities for restores to mess with creative block data.
 * Note that this does not include restore interference with inventories!
 * 
 * @author Thorin
 */
public class RestoreManager extends loggerManager {
	/**
     * Constructor for RestoreManager
     * @param arguments : The arguments of the command
     * @param radius_location : location where command was thrown
     */
	public RestoreManager(String[] arguments, Location radius_location) {
		this.coreprotect = ParamnesticCure.getInstance().getCoreProtect();
    	
    	interpretArguments(arguments,radius_location);
	}
	
	@Override
	public void executeTask() {
		// TODO Auto-generated method stub
		
	}

}
