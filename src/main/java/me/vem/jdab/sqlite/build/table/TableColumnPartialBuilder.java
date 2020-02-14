package me.vem.jdab.sqlite.build.table;

import me.vem.jdab.sqlite.DbType;

public class TableColumnPartialBuilder {

	private final SqliteTableBuilder parent;
	
	private String columnName;
	private DbType columnType;
	private boolean complete = false;
	
	TableColumnPartialBuilder(SqliteTableBuilder parent){
		this.parent = parent;
	}
	
	/**
	 * Required.
	 * @param columnName
	 * @return
	 */
	public TableColumnPartialBuilder setColumnName(String columnName) {
		this.columnName = columnName;
		return this;
	}

	public TableColumnPartialBuilder setColumnType(DbType columnType) {
		this.columnType = columnType;
		return this;
	}
	
	public TableColumnPartialBuilder addColumnContraint() {
		
		return this;
	}
	
	public boolean isComplete() {return complete;}
	public SqliteTableBuilder complete() {
		complete = true;
		return parent;
	}
}
