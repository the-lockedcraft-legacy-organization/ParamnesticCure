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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;

/**
 * @author Frostalf
 */
public class DatabaseCheck {

    private BoneCP boneCP;
    private Connection connection;

    private BoneCPConfig config = null;
    private ParamnesticCure plugin = ParamnesticCure.getInstance();
    private String database;
    private String address;
    private int port;
    private String user;
    private String password;
    private String driver;
    private String url;

    /**
     * Caches and checks the status of a certain database..
     * @param name Name of the plugin this database pertains to.
     * @param database Name of the database being checked.
     * @param address Address of the database being checked.
     * @param port Port on which to communicate with the database being checked.
     * @param user User to use when communicating with the database.
     * @param password Password to be used when communicating with the database as provided user.
     * @param driver Driver to use for this database communication.
     */
    public DatabaseCheck(String name, String database, String address, int port, String user, String password, String driver) {
        this.database = database;
        this.address = address;
        this.port = port;
        if(user == null || user.isBlank()) {
            this.user = "default";
        } else {
            this.user = user;
        }
        this.password = password;
        this.driver = driver;
        final int POOLSIZE = 10;
        final int MAXCONNECT = 20;
        config = new BoneCPConfig();
        config.setPartitionCount(POOLSIZE);
        config.setMaxConnectionsPerPartition(MAXCONNECT);
        config.setUser(this.user);
        config.setPassword(this.password);
        if(this.driver.equalsIgnoreCase("sqlite")) {
            this.url = ("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + this.database + ".db");
            try {
                Class.forName("org.sqlite.JDBC");
                DriverManager.registerDriver(new org.sqlite.JDBC());
                connection = DriverManager.getConnection(this.url);
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            //config.setJdbcUrl("jdbc:"+ driver +":" + plugin.getDataFolder().getPath() + "/" + this.database + ".db");
        } else {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                DriverManager.registerDriver(new com.mysql.jdbc.Driver());
                this.boneCP = new BoneCP(config);
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            config.setJdbcUrl("jdbc:"+ this.driver +"://" + this.address + ":" + this.port + "/" + this.database);
        }
    }

    /**
     * Gets the current database connection.
     * @return Current connection.
     */
    public Connection getConnection() {
        if(connection == null) {
            try {
                if(this.driver.equalsIgnoreCase("sqlite")) {
                    connection = DriverManager.getConnection(this.url);
                    ParamnesticCure.getInstance().getLogger().log(Level.CONFIG, "{0} {1} {2}", new Object[]{this.url, this.user, this.database});
                } else {
                    connection = this.boneCP.getConnection();
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(SEVERE, ex.getMessage(), ex);
            }
        }
        return connection;
    }
}
