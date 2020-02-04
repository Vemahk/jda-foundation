package me.vem.jdab.sqlite;

public class DbParameter {
	private final String name;
	private final DbType type;
	
	public DbParameter(String name, DbType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() { return name; }
	public DbType getType() { return type; }
}