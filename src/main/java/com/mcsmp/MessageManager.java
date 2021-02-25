/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * A class that allows somewhat fancy messaging to the player
 * @author Thorin
 */
public class MessageManager {
	private Player target;
	private String prefix = ChatColor.DARK_GRAY + "<" +ChatColor.GRAY + "" + ChatColor.ITALIC + " Paramnestic " + ChatColor.RESET + "" + ChatColor.DARK_GRAY + "> ";
	private ChatColor errorColor = ChatColor.RED;
	private ChatColor normalColor = ChatColor.WHITE;
	
	boolean isConsole = false;
	boolean hasShownLogo = false;
	
	/**
	 * 
	 * @param player The player that should get the messages
	 * @param ini_type What type of process that the message manager is for
	 */
	MessageManager(Player target,String ini_type){
		this(target);
		sendMessage("Intercepted a " + ini_type + " command",false) ;
	}
	/**
	 * 
	 * @param player The player that should get the messages
	 */
	MessageManager(Player target){
		if(target != null)
			this.target = target;
		else
			isConsole = true;
	}
	/**
	 * Compiles a message depending on if it's for console, the prefix has been shown or not, or if its an error message
	 * @param message
	 * @param isError
	 */
	public void sendMessage(String message, boolean isError) {
		if(isConsole) {
			if(isError)
				ParamnesticCure.getInstance().getLogger().warning( message );
			else
				ParamnesticCure.getInstance().getLogger().info( message );
		}
		else {
			message = ( isError ? errorColor : normalColor) + message;
			if (!hasShownLogo) {
				message = prefix + message;
				hasShownLogo = true;
			}
			target.sendMessage( ChatColor.GRAY + "- "  + message );
		}
	}
}
