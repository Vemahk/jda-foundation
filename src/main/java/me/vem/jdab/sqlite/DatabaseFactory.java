package me.vem.jdab.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import me.vem.jdab.utils.Logger;

public class DatabaseFactory {

    public static final String CONNECTION_PREFIX = "jdbc:sqlite:";
    
    private static Map<String, String> connectionStrings = new HashMap<String, String>();
    private static String defaultConnectionString = null; 
    
    /**
     * The CONNECTION_PREFIX is automatically prepended to the file path in this method.
     * @param key The key to identify this particular connection string when creating database connections.
     * @param filePath The path (relative or absolute) to the file in which the database will reside.
     */
    public static void setupConnectionString(String key, String filePath) {
        setupConnectionString(key, filePath, false);
    }
    
    /**
     * @param key The name of the connection string used when making new connections to the sqlite databases.
     * @param filePath The path (relative or absolute) to the file in which the database will reside.
     * @param defaultConnection Whether to set this connection string as the default connection when creating new database connections.
     */
    public static void setupConnectionString(String key, String filePath, boolean defaultConnection) {
        if(connectionStrings.containsKey(key))
            throw new IllegalArgumentException("Cannot set new connection " + key + " as a connection string by that name already exists.");   
        
        String newConnectionString = CONNECTION_PREFIX + filePath;
        connectionStrings.put(key, newConnectionString);
        
        if(defaultConnection) {
            if(defaultConnectionString != null) {
                Logger.warnf("Connection string [%s] is being overriden as the default connection string with [%s].%n", defaultConnectionString, newConnectionString);   
            }
            
            defaultConnectionString = newConnectionString;
        }
    }
    
    public static Connection create() {
        if(defaultConnectionString == null)
            throw new IllegalArgumentException("Cannot create a database connection to the default database - no default was set.");
        
        return getConnection(defaultConnectionString);
    }
    
    public static Connection create(String connectionName) {
        String connectionString = connectionStrings.get(connectionName); 
        if(connectionString == null)
            throw new IllegalArgumentException("Connection name '" + connectionName + "' was not recognized.");
        
        Connection conn = getConnection(connectionString);
        if(conn == null)
            Logger.errf("A connection could not be established to the '%s' database.", connectionName);
        
        return conn;
    }
    
    private static Connection getConnection(String connectionString) {
        try {
            return DriverManager.getConnection(connectionString);
        }catch(SQLException e) {
            return null;
        }
    }
}
