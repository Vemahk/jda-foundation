package me.vem.jdab.cmd.test;

import java.util.Arrays;

import me.vem.jdab.cmd.Command;
import me.vem.jdab.utils.Respond;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TestSubCommands extends Command{

    private static TestSubCommands instance;
    public static TestSubCommands getInstance() { return instance; }
    public static void initialize() {
        if(instance == null)
            instance = new TestSubCommands("test-sub");
    }
    
    private TestSubCommands(String cmdName) {
        super(cmdName);
        
        TestSubCommand.initialize(this);
        TestHiddenSubCommand.initialize(this);
    }

    @Override
    public boolean run(GuildMessageReceivedEvent event, String... args) {
        if(!super.run(event, args)) return false;

        if(args.length == 0)
            Respond.async(event.getChannel(), "Yep, you can run this. Shush, child.");
        else {
            Command subCmd = this.getSubCommand(args[0]);
            if(subCmd != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCmd.run(event, subArgs);
            }
        }
        
        return true;
    }

    @Override
    public String getDescription() {
        return "A test for if sub commands work... ish.";
    }

    @Override
    public String[] usages() {
        return new String[] {
            "This command is for testing purposes. If you are seeing this, then the Bot dev forgot remove the initalization for one of these commands."
        };
    }

    @Override
    public boolean hasPermissions(Member member, String... args) {
        return true; //For once the auto complete is correct.
    }

    @Override
    protected void unload() {
        instance = null;
    }
}

class TestSubCommand extends Command{
    private static TestSubCommand instance;
    public static TestSubCommand getInstance() { return instance; }
    public static void initialize(Command parent) {
        if(instance == null)
            instance = new TestSubCommand("ni", parent);
    }
    
    private TestSubCommand(String cmdName, Command parent) {
        super(cmdName, parent);
    }

    @Override
    public boolean run(GuildMessageReceivedEvent event, String... args) {
        if(!super.run(event, args)) return false;
        
        Respond.async(event.getChannel(), "This is the 'ni' sub command!");
        
        return true;
    }

    @Override
    public String getDescription() {
        return "This is only for a test. Really, this should never be seen.";
    }

    @Override
    public String[] usages() {
        return new String[] {
            "This command is for testing purposes. If you are seeing this, then the Bot dev forgot remove the initalization for one of these commands."
        };
    }

    @Override
    public boolean hasPermissions(Member member, String... args) {
        if(!this.getParentInstance().hasPermissions(member))
            return false;
        
        return true;
    }

    @Override
    protected void unload() {
        instance = null;
    }
}

class TestHiddenSubCommand extends Command{
    private static TestHiddenSubCommand instance;
    public static TestHiddenSubCommand getInstance() { return instance; }
    public static void initialize(Command parent) {
        if(instance == null)
            instance = new TestHiddenSubCommand("nu", parent);
    }
    
    private TestHiddenSubCommand(String cmdName, Command parent) {
        super(cmdName, parent);
    }

    @Override
    public boolean run(GuildMessageReceivedEvent event, String... args) {
        if(!super.run(event, args)) return false;
        
        Respond.async(event.getChannel(), "This is the 'nu' sub command!");
        
        return true;
    }

    @Override
    public String getDescription() {
        return "This should not be shown.";
    }

    @Override
    public String[] usages() {
        return new String[] {
            "This command is for testing purposes. If you are seeing this, then the Bot dev forgot remove the initalization for one of these commands."
        };
    }

    @Override
    public boolean hasPermissions(Member member, String... args) {
        if(!this.getParentInstance().hasPermissions(member))
            return false;
        
        return false;
    }

    @Override
    protected void unload() {
        instance = null;
    }
}