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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * @author Frostalf
 */
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
        config.setUser(user);
        config.setPassword(password);
        if(driver.equalsIgnoreCase("sqlite")) {
            try {
                Class.forName("org.sqlite.JDBC");
                DriverManager.registerDriver(new org.sqlite.JDBC());
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            config.setJdbcUrl("jdbc:"+ driver +"://" + ParamnesticCure.getInstance().getDataFolder().getPath() + File.pathSeparator + database + ".db");
        } else {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            config.setJdbcUrl("jdbc:"+ driver +"://" + address + ":" + port + "/" + database);
        }
        try {
            this.boneCP = new BoneCP(config);
            createDB();
        } catch (SQLException ex) {
            ParamnesticCure.getInstance().getLogger().log(SEVERE, "Error connection to Database: {0}", ex.getSQLState());
            ParamnesticCure.getInstance().setOK(false);
        }
    }

    /**
     * Gets the current database connection.
     * @return Current connection.
     */
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

    private void createDB() {
        try {
            PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS blocks(id int NOT NULL AUTO_INCREMENT, world varchar(20), x int, y int, z int");
            statement.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
