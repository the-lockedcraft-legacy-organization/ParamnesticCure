/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.mcsmp.ParamnesticCure;
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
			return true;
		}
		
		return player.hasPermission(permission);
	}
}
