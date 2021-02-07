package com.mcsmp;

import org.bukkit.entity.Player;

public class PermissionManager {
	
	
	static public boolean hasRollback(Player player) {
		if(player.hasPermission("coreprotect.rollback"))
			return true;
		return false;
	}
	static public boolean hasPurge(Player player) {
		if(player.hasPermission("coreprotect.purge"))
			return true;
		return false;
	}
	static public boolean hasRestore(Player player) {
		if(player.hasPermission("coreprotect.restore"))
			return true;
		return false;
	}
	static public boolean hasHelp(Player player) {
		if(player.hasPermission("coreprotect.help"))
			return true;
		return false;
	}
}
