/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.database;


import com.mcsmp.DriverEnum;
import com.mcsmp.ParamnesticCure;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import static org.bukkit.Bukkit.getPluginManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Frostalf
 * @author Thorin
 */
public class SqlManager {

    private HikariDataSource boneCP;
    private Connection connection;

    private HikariConfig config = null;
    private ParamnesticCure plugin = ParamnesticCure.getInstance();
    private final String database;
    private final String address;
    private final int port;
    private final String driver;
    private String url;
    private final boolean mysql;
    
    /**
     * Caches and checks the status of a certain database..
     * @param driver Driver to use for this database communication.
     * @param address Address of the database being checked.
     * @param port Port on which to communicate with the database being checked.
     * @param database Name of the database being checked.
     */
    public SqlManager(String driver, String address, int port, String database) {
    	
        this.database = database;
        this.driver = setDriver(driver).toString();
        this.port = port;
        this.address = address;
        if(getDriver() == DriverEnum.SQLITE) {
            try {
                setupSQLITE();
            } catch (SQLException ex) {
                Logger.getLogger(SqlManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            mysql = false;
        } else {
            mysql = true;
            try {
                setupMySQL();
            } catch (SQLException ex) {
                Logger.getLogger(SqlManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Gets the current database connection.
     * @return Current connection.
     * @throws java.sql.SQLException
     */
    public Connection getConnection() throws SQLException {
        if (!mysql) {
            return connection = DriverManager.getConnection(this.url);
        }

        if (mysql) {
            return connection = this.boneCP.getConnection();
        }
        plugin.getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{this.url, this.database, this.connection.toString()});
        return this.connection;
    }

    public DriverEnum setDriver(String driver) {
        if(driver.equalsIgnoreCase("mysql") || driver.equalsIgnoreCase("mariadb")) {
            return DriverEnum.MYSQL;
        } else {
            return DriverEnum.SQLITE;
        }
    }

    public DriverEnum getDriver() {
        return DriverEnum.valueOf(this.driver.toUpperCase());
    }

    public DriverEnum getDriver(String driver) {
        return DriverEnum.valueOf(driver.toUpperCase());
    }

    private void setupMySQL() throws SQLException {
    	//Creates a properties file if it doesn't exist
    	if ( !new File(plugin.getDataFolder(), "hikari.properties").exists() ) {
    		plugin.getLogger().warning("[Startup] hikari.properties file is missing.");
    		plugin.getLogger().info("Providing you with a new properties file");
    		plugin.getDataFolder().mkdirs();
    		plugin.saveResource("hikari.properties", true);
    		getPluginManager().disablePlugin(plugin);
    	}
    	
    	//creates a config based on the properties file
        this.config = new HikariConfig(plugin.getDataFolder()+"/hikari.properties");
        
        
        config.setJdbcUrl("jdbc:"+ this.driver +"://" + this.address + ":" + this.port + "/" + this.database);
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SqlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        
        this.boneCP = new HikariDataSource(config);
    }

    private File dbFile;

    private void setupSQLITE() throws SQLException {
        dbFile = new File(plugin.getDataFolder().getAbsoluteFile(), this.database + ".db");

        this.url = ("jdbc:sqlite:" + dbFile.getAbsoluteFile());
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SqlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new org.sqlite.JDBC());
    }
}
