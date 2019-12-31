package me.vem.jdab.cmd;

import me.vem.jdab.utils.Respond;
import me.vem.jdab.utils.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Uptime extends Command{

	private static Uptime instance;
	public static Uptime getInstance() {
		return instance;
	}
	
	public static void initialize() {
		if(instance == null)
			instance = new Uptime();
	}
	
	private long startTime;
	
	private Uptime() {
		super("uptime");
		
		startTime = System.currentTimeMillis();
	}

	@Override
	public boolean run(GuildMessageReceivedEvent event, String... args) {
		if(!super.run(event, args))
			return false;
		
		Respond.async(event.getChannel(), "This bot has been online for " + Utilities.formatTime(System.currentTimeMillis() - startTime) + ".");
		
		return true;
	}
	
	@Override
	public String[] usages() {
		return new String[] {"`uptime` -- Displays the amount of time that the bot has been up for."};
	}

	@Override
	public String getDescription() {
		return "Prints the time that the bot has been up.";
	}

	@Override
	public boolean hasPermissions(Member member, String... args) {
		return true;
	}

	@Override
	protected void unload() {
		instance = null;
	}
}