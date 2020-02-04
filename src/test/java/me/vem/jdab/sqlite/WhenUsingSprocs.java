package me.vem.jdab.sqlite;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class WhenUsingSprocs {

	private static SqliteDatabase db;
	
	@BeforeClass
	public static void FixtureSetup() {
		SqliteDatabase.setDefaultConnection("test.db");
		db = SqliteDatabase.create();
		
		DbHelper.execNonQuery("DROP TABLE IF EXISTS stored_procedures");
		DbHelper.execNonQuery("DROP TABLE IF EXISTS sproc_test_tbl");
		
		String tblSql = "CREATE TABLE IF NOT EXISTS sproc_test_tbl("
				+ "rec_id INTEGER PRIMARY KEY, "
				+ "txt TEXT"
				+ ");";
		
		Assert.assertTrue("Was not able to create test table.", DbHelper.execNonQuery(tblSql));
	}
	
	@AfterClass
	public static void FixtureTeardown() {
		try {
			DbHelper.execNonQuery("DROP TABLE sproc_test_tbl;");
			
			db.close();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Could not close database connection. Failing Teardown...");
		}
	}
	
	@Test
	public void ThenCreatingSprocWorks() {
		try {
			int sprocId = StoredProcedures.create("test_sproc", "SELECT * FROM sproc_test_tbl");
			Assert.assertTrue("sprocId was not significant.", sprocId > 0);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail("Failed to create sproc.");
		}
	}
	
	@Test
	public void ThenRetrievingSprocWorks() {
		final String sprocName = "test_sproc2";
		final String sprocSql = "SELECT * FROM sproc_test_tbl";
		
		try {
			int sprocId = StoredProcedures.create(sprocName, sprocSql);
			Assert.assertTrue("sprocId was not significant.", sprocId > 0);
			
			String getSproc = StoredProcedures.get(sprocName);
			Assert.assertTrue("Retrieved sproc did not match seeded sproc.", getSproc.equals(sprocSql));
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail("Failed to create sproc.");
		}
	}
	
	@Test
	public void ThenRunningSprocWorks() {
		final String sprocName1 = "test_sproc3";
		final String sprocName2 = "test_sproc4";
		
		final String sprocSql1 = "INSERT INTO sproc_test_tbl (txt) VALUES (@txt);";
		final String sprocSql2 = "SELECT * FROM sproc_test_tbl";
		
		try {
			int sprocId1 = StoredProcedures.create(sprocName1, sprocSql1);
			Assert.assertTrue("SprocId1 was not significant.", sprocId1 > 0);
			
			int sprocId2 = StoredProcedures.create(sprocName2, sprocSql2);
			Assert.assertTrue("SprocId2 was not significant.", sprocId2 > 0);
			Assert.assertTrue("SprocIds were not in order", sprocId1 < sprocId2);
			
			final String txtIn = "What a world!";
			StoredProcedures.execNonQuery(sprocName1, new DbInParameter("@txt", txtIn));
			
			boolean match = false;
			try(SqliteQuery cmd = db.getStoredQuery(sprocName2);
					ResultSet reader = cmd.execReader()){
				
				while(reader.next()) {
					if(reader.getString("txt").contentEquals(txtIn)) {
						match = true;
						break;
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				Assert.fail("Could not get stored query from db.");
			}
			
			Assert.assertTrue("Did not find match of inserted data from selected data.", match);
		}catch(SQLException e) {
			e.printStackTrace();
			Assert.fail("An error occured while trying to create/use sprocs.");
		}
	}
}