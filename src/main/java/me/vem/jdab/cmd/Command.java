package me.vem.jdab.cmd;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.vem.jdab.DiscordBot;
import me.vem.jdab.utils.Logger;
import me.vem.jdab.utils.Respond;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public abstract class Command {

	private static List<Command> commands = new LinkedList<>();
	
	/**
	 * O(n) b/c I am figuring that there won't be an insane amount of commands being registered, so screw efficiency.
	 * @param cmd
	 */
	private static void addCommand(Command cmd) {
		for(Command c : commands)
			if(c.name.equals(cmd.name)) {
				Logger.warnf("Cannot register command '%s' because another command with its name has already been registered.", cmd.getClass().getName());
				return;
			}
		commands.add(cmd);
	}
	
	public static Command getCommand(String cmdname) {
		if(cmdname == null || cmdname.isEmpty())
			return null;
		
		for(Command c : commands)
			if(c.name.equals(cmdname))
				return c;
		return null;
	}
	
	public static String[] getCommandLabels() {
		String[] out = new String[commands.size()];
		int i=0;
		for(Command c : commands)
			out[i++] = c.name;
		
		return out;
	}
	
	public static Iterator<Command> getIter(){
		return commands.iterator();
	}
	
	/**
	 * Calls the unload method on all initialized commands and clears the commands map.
	 */
	public static void unloadAll() {
		for(Command cmd : commands) {
			cmd.unload();
			if(cmd instanceof EventListener)
				DiscordBot.getInstance().removeEventListener(cmd);
		}
		commands.clear();
	}
	
	public static void saveAll() {
		for(Command cmd : commands)
			if(cmd instanceof Configurable)
				((Configurable)cmd).save();
	}
	
	private final String name;
	
	protected Command(String cmdname) {
		this.name = cmdname;
		addCommand(this);
	}
	
	/**
	 * @return A string describing the function of the command.
	 */
	public abstract String getDescription();
	
	/**
	 * This command is, by default, unimplemented to let developers choose which of the two helps,
	 * if either, they want to use. The downside, of course, is if you forget to implement it...
	 * @return The string form of help for this command.
	 */
	public abstract String[] usages();
	
	/**
	 * Purpose of this command is to allow/reject users from using this particular command.
	 * The args passed to this command are designed to be the same as the args passed to the run function. 
	 * This allows developers to build sub-commands into their commands that can have different levels
	 * of permissions.
	 * @param event
	 * @param args The command arguments.
	 * @return true if the member given in the event has sufficient permissions to run this command/sub-command. False otherwise.
	 */
	public abstract boolean hasPermissions(Member member, String... args);
	
	/**
	 * Required postcondition: The command can be reloaded after this method is called.
	 * NOTE: The command class will remove the command instance from the HashMap, you do not need to do that here.
	 * 
	 * In the case of my example commands, their static instance must be set back to null.
	 */
	protected abstract void unload();
	
	/**
	 * The super implementation of this method only checks permissions. Override this method in all implementing classes.
	 * @param event
	 * @param args
	 * @return Generally is meant to return whether the program did what the user intended it to do.<br>
	 * For example: if the user calls the command correctly but lacks permissions, then it fails to do what the user intented, so it would return false.
	 */
	public boolean run(GuildMessageReceivedEvent event, String... args) {
		if(!hasPermissions(event.getMember(), args)) {
			Respond.async(event.getChannel(), "You do not have the permissions to run this command.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Tells the bot to respond in the channel given in the event with the help for this command.
	 * @param event
	 * @return true, always. So you can return this statement in the run() method.
	 */
	public boolean sendHelp(TextChannel channel, boolean successful) {
		EmbedBuilder builder = new EmbedBuilder();
		
		StringBuilder usage = new StringBuilder();
		for(String s : this.usages())
			usage.append(s).append('\n');
		
		builder.setColor(successful ? Color.GREEN : Color.RED)
				.setTitle("Command Help")
				.setDescription(this.name)
				.addField("Description:", this.getDescription(), false)
				.addField("Usages:", usage.toString(), false);
		
		Respond.async(channel, builder.build());
		return successful;
	}
	
	protected String getName() {
		return name;
	}
}
