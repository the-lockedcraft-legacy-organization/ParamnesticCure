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
		return player.hasPermission(configSektion.getString("blockLoggerPermissions.rollback"));
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasPurge(Player player) {
		return player.hasPermission(configSektion.getString("blockLoggerPermissions.purge"));
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasRestore(Player player) {
		return player.hasPermission(configSektion.getString("blockLoggerPermissions.restore"));
	}
	/**
	 * 
	 * @param player
	 * @return
	 */
	static public boolean hasHelp(Player player) {
		return player.hasPermission(configSektion.getString("blockLoggerPermissions.help"));
	}
}
