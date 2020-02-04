package me.vem.jdab.sqlite;

public class DbInParameter{

	private final String key;
	private final Object val;
	
	public DbInParameter(String key, Object val) {
		this.key = key;
		this.val = val;
	}
	
	public String getKey() {
		return key;
	}
	
	public Object getValue() {
		return val;
	}
}
