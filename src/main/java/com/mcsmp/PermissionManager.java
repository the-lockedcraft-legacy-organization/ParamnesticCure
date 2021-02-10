package com.mcsmp;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
/**
 * We all know what this does
 * @author Thorin
 */
public class PermissionManager {
	private static ConfigurationSection configSektion = ParamnesticCure.getInstance().getConfig().getConfigurationSection("");
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasRollback(Player player) {
		return hasPermission(player,configSektion.getString("blockLoggerPermissions.rollback"));
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasPurge(Player player) {
		return hasPermission(player,configSektion.getString("blockLoggerPermissions.purge"));
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasRestore(Player player) {
		return hasPermission(player,configSektion.getString("blockLoggerPermissions.restore"));
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasHelp(Player player) {
		return hasPermission(player,configSektion.getString("blockLoggerPermissions.help"));
	}
	
	static private boolean hasPermission(Player player, String permission) {
		//null => console (i hope)
		if(player == null) {
			ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Registered as a console command");
			return true;
		}
		ParamnesticCure.getInstance().getLogger().info("[Manual Debug] Registered as a player command");
		
		return player.hasPermission(permission);
	}
}