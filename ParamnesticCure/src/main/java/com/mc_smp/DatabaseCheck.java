/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mc_smp;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Frostalf
 */
//TODO make invalid db connections shut down plugin.
public class DatabaseCheck {
    
    private BoneCP boneCP;
    private Connection connection;
    
    private ParamnesticCure plugin;
    final String DBADDRESS = plugin.getConfig().getString("databases.address");
    final int DBPORT = plugin.getConfig().getInt("databases.port");
    final String DBUSER = plugin.getConfig().getString("databases.username");
    final String DBPASS = plugin.getConfig().getString("databases.password");

    public DatabaseCheck(String database) {
        //Makes a new config w/ fed information
        final byte POOLSIZE = 10;
        final byte MAXCONNECT = 20;
        BoneCPConfig config = new BoneCPConfig();
        config.setPassword(DBPASS);
        config.setUser(DBUSER);
        config.setJdbcUrl("jdbc:mysql://" + DBADDRESS + ":" + DBPORT + "/" + database);
        config.setPartitionCount(POOLSIZE);
        config.setMaxConnectionsPerPartition(MAXCONNECT);
        //Makes a connection w/ those details.
        try {
            Class.forName("com.mysql.jdbc.Driver");
            boneCP = new BoneCP(config);
            connection = boneCP.getConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void checkDB() {

    }
    
    //Gets the connection
    public Connection getConnection() {
        if(connection == null) {
            try {
                connection = this.boneCP.getConnection();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return connection;
    }

    public void initDB(String DB) {

    }
}
