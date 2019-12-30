package me.vem.jdab.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.vem.jdab.utils.Logger;

public class DatabaseManager {

    private static final String SQLITE_CONNECTION = "jdbc:sqlite:";
    private static String defaultDbPath = "bot_config.db";
    
    public static void setupDefault(String databasePath) {
        defaultDbPath = databasePath;
    }
    
    private static Connection connect() throws SQLException {
        return connect(defaultDbPath);
    }
    
    private static Connection connect(String dbPath) throws SQLException {
        return DriverManager.getConnection(SQLITE_CONNECTION + dbPath);
    }
    
    /**
     * Will execute the given query.
     * @param sql
     */
    public static void execNonQuery(String sql) {
        try(Connection db = connect();
                Statement cmd = db.createStatement()){
            
            cmd.execute(sql);
        }catch(SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Will execute the given query as a PreparedStatement, inserting the given arguments into the statement as they appear.
     * @param preparedSql
     * @param args
     */
    public static void execNonQuery(String preparedSql, Object... args) {
        try(Connection db = connect();
                PreparedStatement stmt = db.prepareStatement(preparedSql)){
            
            for(int i=0;i<args.length;i++)
                DbType.wrap(args[i]).applyTo(stmt, i + 1);
            
            stmt.execute();
        }catch(SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Will execute the given query and return the first result's first row's first column. This is for queries that ought to expect only a single return value, like a rowid.
     * @param sql
     * @return
     */
    public static Object execScalar(String sql) {
        try(Connection db = connect();
                Statement cmd = db.createStatement();
                ResultSet reader = cmd.executeQuery(sql)){
            
            if(!reader.next())
                throw new SQLException("Query did not return any results!");
            
            return reader.getObject(1);
        }catch(SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Will execute the given query and return the first result's first row's first column. This is for queries that ought to expect only a single return value, like a rowid.
     * Will use the PreparedStatement and insert args into the statement in the order they appear.
     * @param sql
     * @param args
     * @return
     */
    public static Object execScalar(String preparedSql, Object... args) {
        ResultSet reader = null;
        
        try(Connection db = connect();
                PreparedStatement stmt = db.prepareStatement(preparedSql)){
            
            for(int i=0;i<args.length;i++)
                DbType.wrap(args[i]).applyTo(stmt, i + 1);
            
            reader = stmt.executeQuery();
            
            if(!reader.next())
                throw new SQLException("Query did not return any results!");
            
            return reader.getObject(1);
        }catch(SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
            return null;
        }finally {
            try {
                if(reader != null)
                    reader.close();
            } catch (SQLException e) {
                Logger.err(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Will execute a query and return the ResultSet representing the query's returns. ResultSet is not automatically closed - that's up to you, mate.
     * Disclaimer: I don't actually know if this works. My intiuition is that as soon as the Connection is auto-closed, the ResultSet will be useless...
     *  Unless it actually loads all of the query's results into memory when the execute statement is made. We'll see.
     * @param sql
     * @return
     */
    public static ResultSet execResultSet(String sql) {
        try(Connection db = connect();
                Statement cmd = db.createStatement()){
            
            return cmd.executeQuery(sql);
        }catch(SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Will execute a query and return the ResultSet representing the query's results. Will insert args into the PreparedStatement in the order they are given.
     * @param preparedSql
     * @param args
     * @return
     */
    public static ResultSet execResultSet(String preparedSql, Object... args) {
        try(Connection db = connect();
                PreparedStatement stmt = db.prepareStatement(preparedSql)){

            for(int i=0;i<args.length;i++)
                DbType.wrap(args[i]).applyTo(stmt, i + 1);
            
            return stmt.executeQuery();
        }catch(SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
