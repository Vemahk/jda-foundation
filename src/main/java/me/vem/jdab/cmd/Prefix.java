package me.vem.jdab.cmd;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.vem.jdab.utils.ExtFileManager;
import me.vem.jdab.utils.Logger;
import me.vem.jdab.utils.Respond;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Prefix extends Command implements Configurable{
	
	private static final String DEFAULT_PREFIX = "..";
	
	//Modified Singleton Structure
	private static Prefix instance;
	public static Prefix getInstance() { return instance; }
	public static void initialize() {
		if(instance != null) return;
		instance = new Prefix();
	} 
	
	/**
	 * Shorthand for doing Prefix.getInstance().getPrefix(g);
	 * @param g The Guild
	 * @return Guild g's prefix.
	 */
	public static String get(Guild g) { return instance.getPrefix(g); }
	
	private HashMap<Long, String> prefixDatabase;
	
	private Prefix() {
		super("prefix");
		load();
	}

	@Override
	public boolean run(GuildMessageReceivedEvent event, String... args) {
		if(!super.run(event, args)) return false;
		
		if(args.length > 1)
			return super.sendHelp(event.getChannel(), true);
		
		if(args.length == 0) {
			Respond.asyncf(event.getChannel(), "Guild's Current Prefix: `%s`", getPrefix(event.getGuild()));
			return true;
		}
		
		for(char c : args[0].toCharArray())
			if(Character.isWhitespace(c)) {
				Respond.async(event.getChannel(), "Prefix cannot contain whitespace");
				return false;
			}
		
		setPrefix(event.getGuild(), args[0]);
		Respond.asyncf(event.getChannel(), "Guild's prefix set to `%s`", args[0]);
		
		return true;
	}
	
	public void setPrefix(Guild guild, String prefix) {
		prefixDatabase.put(guild.getIdLong(), prefix);
	}
	
	public String getPrefix(Guild guild) {
		if(!prefixDatabase.containsKey(guild.getIdLong()))
			return DEFAULT_PREFIX;
		return prefixDatabase.get(guild.getIdLong());
	}
	
	@Override
	public boolean hasPermissions(Member member, String... args) {
		return member.hasPermission(Permission.ADMINISTRATOR);
	}

	@Override
	public String[] usages() {
		return new String[] {
			"`prefix [newprefix]`",
			" - Responds with the guild's current prefix if no argument is given, or",
			" - Sets the guild's prefix to the given argument."
		};
	}
	
	@Override
	public void save() {
		try {
			PrintWriter out = ExtFileManager.getConfigOutput("prefix.json");
			out.print(ExtFileManager.getGsonPretty().toJson(prefixDatabase));
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Logger.infof("Prefix database saved...");
	}
	
	@Override
	public void load() {
		prefixDatabase = new HashMap<>();
		
		File configFile = ExtFileManager.getConfigFile("prefix.json");
		if(configFile == null) return;
		
		String content = ExtFileManager.readFileAsString(configFile);
		if(content == null || content.length() == 0) return;
		
		Gson gson = ExtFileManager.getGsonPretty();
		prefixDatabase = gson.fromJson(content, new TypeToken<HashMap<Long, String>>(){}.getType());
	}
	
	@Override
	protected void unload() {
		save();
		instance = null;
	}
	@Override
	public String getDescription() {
		return "Sets the prefix to the command that this bot monitors for in your guild.";
	}
}