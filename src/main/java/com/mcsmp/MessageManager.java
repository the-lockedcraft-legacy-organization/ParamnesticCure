package com.mcsmp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jdk.internal.loader.AbstractClassLoaderValue.Sub;

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
		compileMessage("Intercepted a " + ini_type + " command",false) ;
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
	public void actionsBlocks_affected(Integer num_actions, Integer num_blocks) {
		compileMessage(num_actions.toString() + " actions were found",false) ;
		compileMessage(num_blocks.toString() + " blocks were affected",false) ;
	}
	
	public void incorrect_input() {
		compileMessage("Invalid input",true);
	}
	
	public void player_not_found(String playername) {
		compileMessage("Could not find " + playername, true) ;
	}
	/**
	 * Sends a message about the operational time
	 * @param time in seconds
	 */
	public void operationaltime(Double time) {
		compileMessage("Operational time: " + time.toString() + " seconds",false) ;
	}
	
	public void no_actions_found() {
		compileMessage("No actions where found or incorrect input",true) ;
	}
	/**
	 * Compiles a message depending on factors as if it's for console, or if the prefix has been shown or not
	 * @param message
	 * @param isError
	 */
	public void compileMessage(String message, boolean isError) {
		if(isConsole)
			sendMessage( message );
		else {
			message = ( isError ? errorColor : normalColor) + message;
			if (!hasShownLogo)
				message = prefix + message;
			sendMessage( message );
		}
			
	}
	private void sendMessage(String message) {
		if(isConsole)
			ParamnesticCure.getInstance().getLogger().info(message);
		else
			target.sendMessage( ChatColor.GRAY + "- " + message );
	}
	
}
