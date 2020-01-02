package me.vem.jdab.cmd;

import java.awt.Color;
import java.util.Iterator;
import java.util.function.Predicate;

import me.vem.jdab.struct.menu.EmbedMenu;
import me.vem.jdab.utils.Respond;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
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
		
		final Predicate<Command> PERMISSIONS_CHECK = (cmd) -> {
	        return cmd.hasPermissions(event.getMember());
	    };
		
		if(args.length == 0) {
			//WHAT'S THIS?! CALLBACK HELL?!
			event.getAuthor().openPrivateChannel().queue(
		        (pc) -> new HelpMenu(pc, PERMISSIONS_CHECK),
		        (fail) -> new HelpMenu(event.getChannel(), PERMISSIONS_CHECK)
			);
			return true;
		}
		
		Command cmd = Command.getCommand(args[0]);
		if(cmd != null) cmd.sendHelp(event.getChannel(), true);
		else Respond.asyncf(event.getChannel(), "Command `%s` not recognized.", args[0]);;
		
		event.getMessage().delete().queue();
		
		return true;
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
	public boolean hasPermissions(Member member, String... args) {
		return true;
	}
	
	@Override
	protected void unload() {
		instance = null;
	}
	
	private class HelpMenu extends EmbedMenu{
	    private Predicate<Command> filter;
	    
		public HelpMenu(MessageChannel channel) {
			super(channel);
		}
		
		public HelpMenu(MessageChannel channel, Predicate<Command> filter) {
		    super(channel);
		    this.filter = filter;
		}

		@Override
		public MessageEmbed getEmbed(int page) {
			return getPage(page);
		}
	    
	    private MessageEmbed getPage(int page) {
	        EmbedBuilder builder = new EmbedBuilder().setColor(Color.RED).setTitle("Help - Page " + page);
	        
	        if(page < 1)
	            return builder.addField("No such page", "", false).build();
	        
	        Iterator<Command> iter = Command.getIter(filter);
	        for(int i=0; i < (page - 1) * 5;i++, iter.next()) {
	            if(!iter.hasNext())
	                return builder.addField("No such page", "", false).build();
	        }
	        
	        for(int x=0;x<5;x++) {
	            if(!iter.hasNext()) {
	                if(x == 0)
	                    return builder.addField("No such page", "", false).build();
	                break;
	            }
	            Command nxt = iter.next();
	            builder.addField(nxt.getFullName(), nxt.getDescription(), false);
	        }
	        
	        return builder.setColor(Color.GREEN).build();
	    }
	}
}
