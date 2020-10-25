package com.mcsmp.database;

import java.net.InetAddress;
import static java.net.InetAddress.getByName;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
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

    public DataBases(String databaseName, String address, int port, String user, String password, String driver) {
        this.databaseName = databaseName;
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
        this.driver = driver;
        this.databaseConnection = new DatabaseCheck(databaseName, address, port, user, password, driver);
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public InetAddress getAddress() throws UnknownHostException {
       return getByName(this.address);
    }

    public Integer getPort() {
        return this.port;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDriver() {
        return driver;
    }

    public Connection getConnection() {
        return this.databaseConnection.getConnection();
    }

    public ResultSet getResults(String query) throws SQLException {
        return getConnection().createStatement().executeQuery(query);
    }
}
