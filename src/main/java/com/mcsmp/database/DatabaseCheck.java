/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.database;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.mcsmp.ParamnesticCure;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

/**
 * @author Frostalf
 */
//TODO make invalid db connections shut down plugin.
public class DatabaseCheck {

    private BoneCP boneCP;
    private Connection connection;

    private BoneCPConfig config = null;
    private ParamnesticCure plugin;
    private String database;
    private String address;
    private int port;
    private String user;
    private String password;
    private String driver;

    public DatabaseCheck(String name, String database, String address, int port, String user, String password, String driver) {
        //Makes a new config w/ fed information
        this.database = database;
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
        this.driver = driver;
        final int POOLSIZE = 10;
        final int MAXCONNECT = 20;
        config = new BoneCPConfig();
        config.setPartitionCount(POOLSIZE);
        config.setMaxConnectionsPerPartition(MAXCONNECT);
        config.setUser(user);
        config.setPassword(password);
        if(driver.equalsIgnoreCase("sqlite")) {
            config.setJdbcUrl("jdbc:"+ driver +"://" + ParamnesticCure.getInstance().getServer().getPluginManager().getPlugin(name).getDataFolder().getPath() + File.pathSeparator + database);
        } else {
            config.setJdbcUrl("jdbc:"+ driver +"://" + address + ":" + port + "/" + database);
        }

        try {
            this.boneCP = new BoneCP(config);
        } catch (SQLException ex) {
            ParamnesticCure.getInstance().getLogger().log(SEVERE, "Error connection to Database: {0}", ex.getSQLState());
            ParamnesticCure.getInstance().setOK(false);
        }
    }

    //Gets the connection
    public Connection getConnection() {
        if(connection == null) {
            try {
                connection = this.boneCP.getConnection();
            } catch (SQLException ex) {
                getLogger(DatabaseCheck.class.getName()).log(SEVERE, null, ex);
            }
        }
        return connection;
    }
}
