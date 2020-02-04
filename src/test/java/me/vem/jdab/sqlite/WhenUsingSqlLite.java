package me.vem.jdab.sqlite;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class WhenUsingSqlLite {

	private static SqliteDatabase db;
	
	@BeforeClass
	public static void FixtureSetup() {
		SqliteDatabase.setDefaultConnection("test.db");
		db = SqliteDatabase.create();
		
		String sql = "CREATE TABLE IF NOT EXISTS when_using_sqlite("
                + "rec_id INTEGER PRIMARY KEY, "
                + "null_col TEXT, "
                + "text_col TEXT, "
                + "int_col INTEGER, "
                + "real_col REAL, "
                + "blob_col BLOB"
               + ");";
		
		try(SqliteQuery cmd = db.getQuery(sql)){
			cmd.execNonQuery();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			Assert.fail("Failed to set up table for testing.");
		}
	}
	
	@AfterClass
	public static void FixtureTeardown() {
		try {
			try(SqliteQuery cmd = db.getQuery("DROP TABLE when_using_sqlite;")){
				cmd.execNonQuery();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
			db.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			Assert.fail("Failed to close database connection");
		}
	}
	
	@Test
	public void ThenUsingNoParametersWorks() {
		String str = "Greetings, World!";
		
		String insertSql = "INSERT INTO when_using_sqlite (text_col) VALUES ('" + str + "');";
		
		try (SqliteQuery command = db.getQuery(insertSql)){
			command.execNonQuery();
		}catch(IOException | SQLException e) {
			e.printStackTrace();
		}
		
		String selectSql = "SELECT rec_id FROM when_using_sqlite WHERE text_col = '" + str + "' LIMIT 1;";
		try(SqliteQuery command = db.getQuery(selectSql)){
			Integer id = (Integer)command.execScalar();
			Assert.assertNotNull("no results were returned.", id);
			Assert.assertTrue("rec_id (" + id + ") was not significant", id > 0);
		}catch(IOException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void ThenUsingSingleParameterWorks() {
		String str = "Hello, World!";
		
		try(SqliteQuery command = db.getQuery("INSERT INTO when_using_sqlite (text_col) VALUES (@txt);")){
			command.addParameter("@txt", str);
			command.execNonQuery();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		
		try(SqliteQuery command = db.getQuery("SELECT rec_id FROM when_using_sqlite WHERE text_col = @txt LIMIT 1")){
			command.addParameter("@txt", str);
			Integer id = (Integer) command.execScalar();
			Assert.assertNotNull("no results were returned.", id);
			Assert.assertTrue("rec_id ("+id+") was not valid.", id > 0);
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			Assert.fail("An SQL error occurred in the test.");
		}
	}
	
	@Test
	public void ThenUsingMultipleParametersWorks() {
		String str1 = "Salutations, World!";
		String str2 = "Goodbye, World!";
		
		String sql = "INSERT INTO when_using_sqlite (text_col) VALUES (@txt_a), (@txt_b)";
		try(SqliteQuery command = db.getQuery(sql)){
			command.addParameter("@txt_a", str1);
			command.addParameter("@txt_b", str2);
			command.execNonQuery();
		}catch(IOException | SQLException e) {
			e.printStackTrace();
		}
		
		sql = "SELECT rec_id, text_col FROM when_using_sqlite WHERE text_col = @txt_a OR text_col = @txt_b ORDER BY rec_id";
		try(SqliteQuery command = db.getQuery(sql)){
			command.addParameter("@txt_a", str1);
			command.addParameter("@txt_b", str2);
			
			try(ResultSet reader = command.execReader()){
				Assert.assertTrue("Reader returned no results", reader.next());
				Integer id1 = reader.getInt("rec_id");
				String txt1 = reader.getString("text_col");
				
				Assert.assertNotNull("1st rec_id returned null", id1);
				Assert.assertNotNull("1st txt returned null", txt1);
				Assert.assertTrue("id1 was not significant", id1 > 0);
				Assert.assertTrue("txt1 (" + txt1 +") did not match str1 ("+ str1 +")", txt1.equals(str1));
				
				Assert.assertTrue("Reader only returned a single result.", reader.next());
				Integer id2 = reader.getInt("rec_id");
				String txt2 = reader.getString("text_col");
				
				Assert.assertNotNull("2nd rec_id returned null", id2);
				Assert.assertNotNull("2nd txt returned null", txt2);
				Assert.assertTrue("id2 was not significant", id2 > 0);
				Assert.assertTrue("txt2 (" + txt2 + ") did not match str2 (" + str2 + ")", txt2.equals(str2));
				
				Assert.assertTrue("Ids were not consecutive (id1: "+id1+", id2: "+id2+")", id2 == id1 + 1);
				Assert.assertFalse("More results were returned by reader then were expected", reader.next());	
			}catch(SQLException e) {
				e.printStackTrace();
				Assert.fail("Failed to return ResultSet from query.");
			}
		}catch(IOException e) {
			e.printStackTrace();
			Assert.fail("A problem occurred while retrieving the query.");
		}
	}
}