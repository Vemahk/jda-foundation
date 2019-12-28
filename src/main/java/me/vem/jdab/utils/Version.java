package me.vem.jdab.utils;

public class Version {
	
	private static Version instance;
	public static Version getVersion() { return instance; }
	public static void initialize(int major, int minor, int rev, int build, String name) {
		if(instance == null)
			instance = new Version(major, minor, rev, build, name);
	}
	
	private int major, minor, rev, build;
	private String name;
	
	private Version(int major, int minor, int rev, int build, String name) {
		this.major = major;
		this.minor = minor;
		this.rev = rev;
		this.build = build;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return String.format("%s[%d.%d.%d_%d]", name, major, minor, rev, build);
	} //e.g. 3.2.4_512
	
}
