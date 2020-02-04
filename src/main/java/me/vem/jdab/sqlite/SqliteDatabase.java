package me.vem.jdab.sqlite;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SqliteDatabase implements Closeable{

	private static final String SQLITE_CONN = "jdbc:sqlite:";
	private static String DB_PATH = "bot_config.db";
	
	private static Map<String, SqliteDatabase> activeDatabases;
	
	public synchronized static SqliteDatabase create() {
		return create(DB_PATH);
	}
	
	public synchronized static SqliteDatabase create(String dbPath) {
		if(activeDatabases == null)
			activeDatabases = new HashMap<>();
		
		SqliteDatabase activeInstance = activeDatabases.get(dbPath);
		
		try {
			if(activeInstance == null || activeInstance.isClosed) {
				activeDatabases.put(dbPath, activeInstance = new SqliteDatabase(dbPath));
				return activeInstance;
			}
			
			return activeInstance;
		}catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized static void setDefaultConnection(String dbPath) {
		DB_PATH = dbPath;
	}
	
	private final Connection conn;
	private boolean isClosed;
	
	private SqliteDatabase() throws SQLException{
		this(DB_PATH);
	}
	
	private SqliteDatabase(String databasePath) throws SQLException{
		this.conn = DriverManager.getConnection(SQLITE_CONN + databasePath);
		this.isClosed = false;
	}

	Connection getConnection() { return conn; }

	public SqliteQuery getQuery(String sql) {
		return new SqliteQuery(this, sql);
	}
	
	public SqliteQuery getStoredQuery(String sprocName) {
		return new SqliteQuery(this, StoredProcedures.get(sprocName));
	}
	
	@Override
	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			this.isClosed = true;	
		}
	}
}
