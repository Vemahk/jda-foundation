package me.vem.jdab.sqlite;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

public class SqliteQuery implements Closeable{

	private final SqliteDatabase db;
	
	private final Set<String> keySet;
	private final PriorityQueue<ParameterInstance> instanceQueue;
	private final String sql;

	private PreparedStatement sqlStmt;
	private boolean isClosed = false;
	
	public SqliteQuery(SqliteDatabase db, String sql) {
		this.db = db;
		keySet = new HashSet<>();
		instanceQueue = new PriorityQueue<>();
		this.sql = sql;
	}
	
	public boolean isClosed() {
		return isClosed;
	}
	
	public SqliteQuery addParameter(String key, Object val) {
		if (keySet.contains(key))
			throw new IllegalArgumentException("VPreparedStatement already comtains an in-parameter for " + key);

		instanceQueue.add(new ParameterInstance(key, val, this.sql));
		keySet.add(key);
		
		return this;
	}
	
	public void execNonQuery() throws SQLException {
		prepare().execute();
	}
	
	public Object execScalar() throws SQLException {
		try(ResultSet reader = prepare().executeQuery()){

			if(!reader.next())
                throw new SQLException("Query did not return any results!");
			
			return reader.getObject(1);
		}
	}
	
	public ResultSet execReader() throws SQLException {
		return prepare().executeQuery();
	}
	
	private PreparedStatement prepare() throws SQLException {
		if(sqlStmt != null || isClosed())
			throw new IllegalStateException("SQL Statement has already been prepared or closed. Cannot be reprepared.");

		String pSql = sql;
		for(String s : keySet)
			pSql = pSql.replace(s, "?");
		
		PreparedStatement pStmt = db.getConnection().prepareStatement(pSql);
		
		int i=1;
		while(!instanceQueue.isEmpty()) {
			ParameterInstance instance = instanceQueue.poll();
			
			instance.val.applyTo(pStmt, i++);
			
			instance.instanceIndecies.pop();
			if(!instance.instanceIndecies.isEmpty())
				instanceQueue.add(instance);
		}
		
		return sqlStmt = pStmt;
	}

	private static class ParameterInstance implements Comparable<ParameterInstance>{
		//final String key;
		final DbTypeInstance val;
		final LinkedList<Integer> instanceIndecies;

		public ParameterInstance(String key, Object val, String sql) {
			//this.key = key;
			this.val = DbType.wrap(val);
			
			this.instanceIndecies = new LinkedList<>();

			int i = sql.indexOf(key, 0);
			for (;i >= 0; i = sql.indexOf(key, i+1))
				instanceIndecies.add(i);
			
			if(instanceIndecies.isEmpty())
				throw new IllegalArgumentException("No instances of '" + key + "' found in the sql statement.");
		}

		@Override
		public int compareTo(ParameterInstance o) {
			if(this.instanceIndecies.isEmpty())
				return 1;
			
			if(o.instanceIndecies.isEmpty())
				return -1;
			
			return this.instanceIndecies.peek() - o.instanceIndecies.peek();
		}
	}

	@Override
	public void close() throws IOException {
		if(isClosed())
			throw new IllegalStateException("Cannot close SQL query- it has already been closed!");
		
		if(sqlStmt != null)
			try {
				sqlStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
		isClosed = true;
	}
}