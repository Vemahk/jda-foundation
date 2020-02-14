package me.vem.jdab.sqlite.build.table;

import me.vem.jdab.sqlite.DbType;

public class SqliteTableBuilder {

	private boolean temp, checkExists;
	private String schemaName, tableName;
	
	public SqliteTableBuilder() {}
	
	/**
	 * Required.
	 * @param tableName
	 * @return
	 */
	public SqliteTableBuilder setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}
	
	public TableColumnPartialBuilder addColumn(String columnName, DbType columnType) {
		return new TableColumnPartialBuilder(this).setColumnName(columnName).setColumnType(columnType);
	}
	
	public SqliteTableBuilder setSchemaName(String schemaName) {
		this.schemaName = schemaName;
		return this;
	}
	
	public SqliteTableBuilder setTemporary() {
		temp = true;
		return this;
	}
	
	public SqliteTableBuilder onlyIfNotExists() {
		checkExists = true;
		return this;
	}
	
	public void create() {
		
	}
}