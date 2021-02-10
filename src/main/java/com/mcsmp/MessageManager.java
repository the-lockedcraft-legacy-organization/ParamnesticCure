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
	private ChatColor normalColor = ChatColor.AQUA;
	
	boolean isConsole = false;
	
	/**
	 * 
	 * @param player The player that should get the messages
	 * @param ini_type What type of process that the message manager is for
	 */
	MessageManager(Player player,String ini_type){
		if(player != null)
			this.target = player;
		else
			isConsole = true;
		sendMessage( compileMessage_logo("Intercepted a " + ini_type + " command",false) );
	}
	/**
	 * 
	 * @param player The player that should get the messages
	 */
	MessageManager(Player player){
		this.target = player;
	}
	public void actionsBlocks_affected(Integer num_actions, Integer num_blocks) {
		sendMessage( compileMessage(num_actions.toString() + " actions were found",false) );
		sendMessage( compileMessage(num_blocks.toString() + " blocks were affected",false) );
	}
	
	public void player_not_found(String playername) {
		sendMessage( compileMessage("Could not find " + playername, true) );
	}
	/**
	 * Sends a message about the operational time
	 * @param time in seconds
	 */
	public void operationaltime(Double time) {
		sendMessage( compileMessage("Operational time: " + time.toString() + " seconds",false) );
	}
	
	public void no_actions_found() {
		sendMessage( compileMessage("No actions where found or incorrect input",true) );
	}
	/**
	 * Compiles a message with the paramnestic logo
	 * @param message
	 * @param error
	 */
	private String compileMessage_logo(String message, boolean error) {
		return prefix + ( error ? errorColor : normalColor) + message;
	}
	/**
	 * Compiles a message without paramnestic logo
	 * @param message
	 * @param error
	 */
	private String compileMessage(String message, boolean error) {
		return ( error ? errorColor : normalColor) + message;
	}
	private void sendMessage(String message) {
		if(isConsole)
			ParamnesticCure.getInstance().getLogger().info(message);
		else
			target.sendMessage( message );
	}
	
}
