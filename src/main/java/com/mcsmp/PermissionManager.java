package com.mcsmp;

import org.bukkit.entity.Player;
/**
 * We all know what this does
 * @author Thorin
 */
public class PermissionManager {
	
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasRollback(Player player) {
		return player.hasPermission("coreprotect.rollback");
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasPurge(Player player) {
		return player.hasPermission("coreprotect.purge");
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasRestore(Player player) {
		return player.hasPermission("coreprotect.restore");
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasHelp(Player player) {
		return player.hasPermission("coreprotect.help");
	}
}
