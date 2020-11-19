/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp.database;

import java.net.InetAddress;
import static java.net.InetAddress.getByName;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Frostalf
 */


public class DataBases {

    private String databaseName;
    private String address;
    private int port;
    private String user;
    private String password;
    private String driver;
    private DatabaseCheck databaseConnection;

    /**
     * Connects to a certain database.
     * @param name Name of the plugin this database pertains to.
     * @param databaseName Name of the database being checked.
     * @param address Address of the database being checked.
     * @param port Port on which to communicate with the database being checked.
     * @param user User to use when communicating with the database.
     * @param password Password to be used when communicating with the database as provided user.
     * @param driver Driver to use for this database communication.
     */
    public DataBases(String name, String databaseName, String address, int port, String user, String password, String driver) {
        this.databaseName = databaseName;
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
        this.driver = driver.toLowerCase();
        this.databaseConnection = new DatabaseCheck(name, databaseName, address, port, user, password, driver);
    }

    /**
     * Checks status of database being interacted with.
     * @return Name of database in question.
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Checks status of database being interacted with.
     * @return Address of database in question.
     * @throws java.net.UnknownHostException if the host being connected to is unknown.
     */
    public InetAddress getAddress() throws UnknownHostException {
       return getByName(this.address);
    }

    /**
     * Checks status of database being interacted with.
     * @return Port of database in question.
     */
    public Integer getPort() {
        return this.port;
    }

    /**
     * Checks status of database being interacted with.
     * @return User of database in question.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Checks status of database being interacted with.
     * @return Password of database in question.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Checks status of database being interacted with.
     * @return Driver being used for connection.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Check database connection.
     * @return Current database connection.
     * @throws java.sql.SQLException
     */
    public Connection getConnection() throws SQLException {
        return this.databaseConnection.getConnection();
    }

    /**
     * Queries the current database.
     * @param query SQL query expression.
     * @return returns the results of the SQL query.
     * @throws java.sql.SQLException any error with the sql query
     */
    public ResultSet getResults(String query) throws SQLException {
        return getConnection().createStatement().executeQuery(query);
    }
}
