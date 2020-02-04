package me.vem.jdab.sqlite;

import java.io.IOException;
import java.sql.SQLException;

import org.jetbrains.annotations.NotNull;

public class DbHelper {

	public static boolean execNonQuery(@NotNull String sql, DbInParameter... params) {
		if(sql == null || sql.length() == 0)
			throw new IllegalArgumentException("Cannot submit a null or empty sql query.");
		
		SqliteDatabase db = SqliteDatabase.create();
		try (SqliteQuery cmd = db.getQuery(sql)){
			for(DbInParameter param : params)
				cmd.addParameter(param.getKey(), param.getValue());
			
			cmd.execNonQuery();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static Object execScalar(@NotNull String sql, DbInParameter... params) {
		if(sql == null || sql.length() == 0)
			throw new IllegalArgumentException("Cannot submit a null or empty sql query.");
		
		SqliteDatabase db = SqliteDatabase.create();
		try (SqliteQuery cmd = db.getQuery(sql)){
			for(DbInParameter param : params)
				cmd.addParameter(param.getKey(), param.getValue());
			
			return cmd.execScalar();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private DbHelper() {}
}