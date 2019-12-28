package me.vem.jdab.cmd;

import java.awt.Color;
import java.util.Iterator;

import me.vem.jdab.struct.menu.EmbedMenu;
import me.vem.jdab.utils.Respond;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Help extends Command{

	private static Help instance;
	public static Help getInstance() { return instance; }
	public static void initialize() {
		if(instance == null)
			instance = new Help();
	} 

	
	private Help() { super("help"); }

	
	@Override
	public boolean run(GuildMessageReceivedEvent event, String... args) {
		if(!super.run(event, args)) return false;
		
		if(args.length == 0) {
			//WHAT'S THIS?! CALLBACK HELL?!
			event.getAuthor().openPrivateChannel().queue((pc) -> {
			    pc.sendMessage(getCommandList(1).build()).queue((msg) -> new HelpMenu(msg));
			}, (fail) -> {
			    Respond.async(event.getChannel(), getCommandList(1).build(), (msg) -> new HelpMenu(msg));
			});
			return true;
		}
		
		Command cmd = Command.getCommand(args[0]);
		if(cmd != null) cmd.sendHelp(event.getChannel(), true);
		else new HelpMenu(Respond.sync(event.getChannel(), getCommandList(1).appendDescription(" - Command not recognized.")));
		
		event.getMessage().delete().queue();
		
		return true;
	}
	
	private EmbedBuilder getCommandList(int page) {
		EmbedBuilder builder = new EmbedBuilder().setColor(Color.RED).setTitle("Help - Page " + page);
		
		if(page < 1)
			return builder.addField("No such page", "", false);
		
		Iterator<Command> iter = Command.getIter();
		for(int i=0; i < (page - 1) * 5;i++, iter.next())
			if(!iter.hasNext())
				return builder.addField("No such page", "", false);
		
		for(int x=0;x<5;x++) {
			if(!iter.hasNext()) {
				if(x == 0)
					return builder.addField("No such page", "", false);
				break;
			}
			Command nxt = iter.next();
			builder.addField(nxt.getName(), nxt.getDescription(), false);
		}
		
		return builder.setColor(Color.GREEN);
	}
	
	@Override
	public String getDescription() {
		return "Prints a list of known commands, or tries and get the help for a specific command.";
	}
	
	@Override
	public String[] usages() {
		return new String[] {
			"`help [command]`",
			" - Prints the help for the given command, or",
			" - Prints a list of commands if no command is mentioned."
		};
	}

	@Override
	public boolean hasPermissions(GuildMessageReceivedEvent event, String... args) {
		return true;
	}
	
	@Override
	protected void unload() {
		instance = null;
	}
	
	private class HelpMenu extends EmbedMenu{
		public HelpMenu(Message msg) {
			super(msg);
		}

		@Override
		public MessageEmbed getEmbed(int page) {
			return getCommandList(page).build();
		}
	}
}
