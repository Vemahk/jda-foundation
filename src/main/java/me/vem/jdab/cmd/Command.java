package me.vem.jdab.cmd;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.function.Predicate;

import me.vem.jdab.DiscordBot;
import me.vem.jdab.utils.Logger;
import me.vem.jdab.utils.Respond;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public abstract class Command {

	private static List<Command> commands = new LinkedList<Command>() {
        private static final long serialVersionUID = -5452155871389714051L;
        
        @Override
        public Iterator<Command> iterator(){
            return new Iterator<Command>() {
                private Stack<Iterator<Command>> iterStack = new Stack<>();
                private Iterator<Command> curIter = listIterator();
                
                @Override
                public boolean hasNext() {
                    while(!curIter.hasNext() && !iterStack.isEmpty())
                        curIter = iterStack.pop();
                    
                    return curIter.hasNext();
                }

                @Override
                public Command next() {
                    if(!hasNext())
                        throw new NoSuchElementException();
                    
                    Command next = curIter.next();
                    Iterator<Command> subCmdIter = next.getSubCommandIterator();
                    
                    if(subCmdIter.hasNext()) {
                        iterStack.push(curIter);
                        curIter = subCmdIter;
                    }
                    
                    //Logger.debugf("CMD List Iterator > %s", next.getFullName());
                    
                    return next;
                }
            };
        }
	};
	
	/**
	 * O(n) b/c I am figuring that there won't be an insane amount of commands being registered, so screw efficiency.
	 * @param cmd
	 */
	private static void addRootCommand(Command cmd) {
		for(Command c : commands)
			if(c.getFullName().equals(cmd.getName())) {
				Logger.warnf("Cannot register command '%s' because another command with its name has already been registered.", cmd.getClass().getName());
				return;
			}
		commands.add(cmd);
	}
	
	public static Command getCommand(String cmdname) {
		if(cmdname == null || cmdname.isEmpty())
			return null;
		
		for(Command c : commands)
			if(c.getFullName().equals(cmdname))
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
		return getIter(null);
	}
	
	public static Iterator<Command> getIter(Predicate<Command> filter){
	    Iterator<Command> baseIter = commands.iterator();
	    if(filter == null)
	        return baseIter;
	    
	    return new Iterator<Command>() {
	        private Command next;
	        
            @Override
            public boolean hasNext() {
                if(next == null)
                    return calcNext() != null;
                
                return true;
            }

            @Override
            public Command next() {
                if(next == null && calcNext() == null)
                    throw new NoSuchElementException();
                
                Command ret = next;
                calcNext();
                return ret;
            }
	        
            private Command calcNext() {
                if(!baseIter.hasNext())
                    return next = null;
                
                Command potentialNext = baseIter.next();
                
                while(!filter.test(potentialNext)) {
                    if(baseIter.hasNext())
                        potentialNext = baseIter.next();
                    else return next = null;
                }
                
                return next = potentialNext;
            }
	    };
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
	
	private final Command parentCommand;
	private final List<Command> subCommands;
	
	private final String name;
	
	protected Command(String cmdName) {
	    this(cmdName, null);
	}
	
	protected Command(String cmdName, Command parentCommand) {
	    this.parentCommand = parentCommand;
	    this.subCommands = new LinkedList<>();
	    
		this.name = cmdName;
		
		if(parentCommand == null)
		    addRootCommand(this);
		else parentCommand.registerSubCommand(this);
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
	
	protected Command getParentInstance() {
	    return parentCommand;
	}
	
	protected Command getSubCommand(String cmdName) {
        if(cmdName == null || cmdName.isEmpty())
            return null;
        
        for(Command c : subCommands)
            if(c.name.equals(cmdName))
                return c;
        
        return null;
    }
	
	protected String getFullName() {
	    if(parentCommand == null)
	        return getName();
	    return parentCommand.getFullName() + "." + getName();
	}
	
	protected String getName() {
		return name;
	}
	
	private Iterator<Command> getSubCommandIterator(){
	    return subCommands.iterator();
	}
	
	private void registerSubCommand(Command cmd) {
	    for(Command c : subCommands)
            if(c.name.equals(cmd.name)) {
                Logger.warnf("Cannot register '%s' as a subcommand of '%s' because another subcommand with its name (%s) has already been registered.", cmd.getClass().getName(), this.getClass().getName(), cmd.getName());
                return;
            }
	    subCommands.add(cmd);
	}
}
