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
	
	/**
	 * 
	 * @param player The player that should get the messages
	 * @param ini_type What type of process that the message manager is for
	 */
	MessageManager(Player player,String ini_type){
		this.target = player;
		sendMessage_logo("Intercepted a " + ini_type + " command",false);
	}
	/**
	 * 
	 * @param player The player that should get the messages
	 */
	MessageManager(Player player){
		this.target = player;
	}
	public void actionsBlocks_affected(Integer num_actions, Integer num_blocks) {
		sendMessage(num_actions.toString() + " actions were found",false);
		sendMessage(num_blocks.toString() + " blocks were affected",false);
	}
	
	public void player_not_found(String playername) {
		sendMessage("Could not find " + playername, true);
	}
	/**
	 * Sends a message about the operational time
	 * @param time in seconds
	 */
	public void operationaltime(Double time) {
		sendMessage("Operational time: " + time.toString() + " seconds",false);
	}
	
	public void no_actions_found() {
		sendMessage("No actions where found or incorrect input",true);
	}
	/**
	 * Sends a message with the paramnestic logo
	 * @param message
	 * @param error
	 */
	private void  sendMessage_logo(String message, boolean error) {
		target.sendMessage(prefix + ( error ? errorColor : normalColor) + message );
	}
	/**
	 * Sends a message without paramnestic logo
	 * @param message
	 * @param error
	 */
	public void sendMessage(String message, boolean error) {
		target.sendMessage( ( error ? errorColor : normalColor) + message );
	}
}
