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
public class DatabaseCheck {

    private BoneCP boneCP;
    private Connection connection;

    public DatabaseCheck(String database, String password, String user, String host, int port, int poolSize, int maxConnections) {
        BoneCPConfig config = new BoneCPConfig();
        config.setPassword(password);
        config.setUser(user);
        config.setJdbcUrl("jdbc:mysql://"+ host + "/" + database);
        config.setPartitionCount(poolSize);
        config.setMaxConnectionsPerPartition(maxConnections);
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
