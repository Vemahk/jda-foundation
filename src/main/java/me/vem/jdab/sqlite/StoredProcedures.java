package me.vem.jdab.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * INCOMPLETE
 */
public class StoredProcedures {

    private static final String SPROC_TABLE = "stored_procedures";
    
    private static boolean tableEnsured = false;
    private static void ensureSprocTable() {
        if(tableEnsured) return;
        
        String sql = "CREATE TABLE IF NOT EXISTS " + SPROC_TABLE + "("
                    + "sproc_id INTEGER PRIMARY KEY, "
                    + "sproc_nm TEXT NOT NULL, "
                    + "sproc TEXT NOT NULL"
                   + ");";
        
        DatabaseManager.execNonQuery(sql);
        tableEnsured = true;
    }
    
    /**
     * 
     * @param sprocName
     * @return The full SQL statement that the sproc name represents.
     */
    public static String get(String sprocName) {
        ensureSprocTable();
        
        
        
        return null;
    }
    
    /**
     * @param sprocName The name of the stored procedure to be inserted.
     * @param sql
     * @return the sproc_id
     * @throws SQLException 
     */
    public static int create(String sprocName, String sql) throws SQLException {
        ensureSprocTable();
        
        String insertSql = ""//TODO
                + "SELECT last_insert_rowid();";
        
        Object res = DatabaseManager.execScalar(insertSql, sprocName, sql);
        
        if(res == null) 
            throw new SQLException("An error occured while created the sproc: " + sprocName);
        
        Integer id = (Integer) res;
        
        return id;
    }
    
    public static void execNonQuery(String sprocName) {
        DatabaseManager.execNonQuery(get(sprocName));
    }
    
    public static Object execScalar(String sprocName) {
        return DatabaseManager.execScalar(get(sprocName));
    }
    
    public static ResultSet execResultSet(String sprocName) {
        return DatabaseManager.execResultSet(get(sprocName));
    }
}
