package com.mcsmp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Thorin
 */
public class MessageManager {
	private Player target;
	private String prefix = ChatColor.LIGHT_PURPLE + "[" +ChatColor.GRAY + "" + ChatColor.ITALIC + " Paramnestic " + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "] ";
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
	 * Compiles a message depending on factors as if it's for console, or if the prefix has been shown or not
	 * @param message
	 * @param isError
	 */
	public void sendMessage(String message, boolean isError) {
		if(isConsole)
			ParamnesticCure.getInstance().getLogger().info( message );
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
