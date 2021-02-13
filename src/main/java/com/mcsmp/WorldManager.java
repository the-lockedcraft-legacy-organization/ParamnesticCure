/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.World;

/**
 * Keeps track of every world and changes the paramnestic database accordingly
 * @author Thorin
 *
 */
public class WorldManager {
	
	static public void addWorldToDB(World world) {
		try {
			Connection connection = ParamnesticCure.getInstance().getConnection();
    		PreparedStatement statement = connection.prepareStatement(
			"INSERT INTO worlds (world)"
			+ " VALUES (?)"
    				);
    		statement.setString(1, world.getName());
    		statement.execute();
		}catch(SQLException ex) {}
	}
	
	static public Integer getWorldId(String worldname) {
		Integer output = -1;
		try {
			Connection connection = ParamnesticCure.getInstance().getConnection();
    		PreparedStatement statement = connection.prepareStatement(
			"SELECT world_id FROM worlds"
			+ " WHERE world = ?"
    				);
    		statement.setString(1, worldname);
    		ResultSet set = statement.executeQuery();
    		if(set.next())
    			output = set.getInt(1);
    		
		}catch(SQLException ex) {}
		return output;
	}
}
