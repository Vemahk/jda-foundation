package me.vem.jdab.sqlite;

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
                   + "); ";
        
        DbHelper.execNonQuery(sql);
        
        sql = "CREATE UNIQUE INDEX IF NOT EXISTS idx_sproc_nm ON " + SPROC_TABLE + " (sproc_nm);";
        DbHelper.execNonQuery(sql);
        
        tableEnsured = true;
    }
    
    /**
     * 
     * @param sprocName
     * @return The full SQL statement that the sproc name represents.
     */
    public static String get(String sprocName) {
        ensureSprocTable();
        
        Object result = DbHelper.execScalar("SELECT sproc FROM " + SPROC_TABLE + " WHERE sproc_nm = @sproc_name LIMIT 1",
        		new DbInParameter("@sproc_name", sprocName));
        
        if(result == null)
        	throw new IllegalArgumentException("No sproc exists by the name: " + sprocName);
        
        return (String)result;
    }
    
    /**
     * @param sprocName The name of the stored procedure to be inserted.
     * @param sql
     * @return the sproc_id
     * @throws SQLException 
     */
    public static int create(String sprocName, String sql) throws SQLException {
        ensureSprocTable();
        
        String insertSql = "INSERT INTO " + SPROC_TABLE + " (sproc_nm, sproc) "
        				 + "SELECT @sproc_nm, @sproc_sql "
        				 + "WHERE NOT EXISTS (SELECT * FROM " + SPROC_TABLE + " WHERE sproc_nm = @sproc_nm);";
        
        DbHelper.execNonQuery(insertSql, 
        		new DbInParameter("@sproc_nm", sprocName), 
        		new DbInParameter("@sproc_sql", sql));
        
        Object res = DbHelper.execScalar("SELECT last_insert_rowid();");
        
        if(res == null) 
            throw new SQLException("An error occured while created the sproc: " + sprocName + ". It may already exist.");
        
        return (Integer) res;
    }
    
    public static void execNonQuery(String sprocName, DbInParameter... params) {
        DbHelper.execNonQuery(get(sprocName), params);
    }
    
    public static Object execScalar(String sprocName, DbInParameter... params) {
        return DbHelper.execScalar(get(sprocName), params);
    }
}