package me.vem.jdab.cmd.test;

import me.vem.jdab.cmd.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TestFilterCommand extends Command{

    private static TestFilterCommand instance;
    public static TestFilterCommand getInstance() { return instance; }
    public static void initialize() {
        if(instance == null)
            instance = new TestFilterCommand("test-filter");
    }
    
    private TestFilterCommand(String cmdName) {
        super(cmdName);
    }

    @Override
    public boolean run(GuildMessageReceivedEvent event, String... args) {
        if(!super.run(event, args)) return false;
        
        return true;
    }

    @Override
    public String getDescription() {
        return "This is only for a test. Really, this should never be seen.";
    }

    @Override
    public String[] usages() {
        return new String[] {
            "No usages are supported in this command. Please, pretend you never saw this"
        };
    }

    @Override
    public boolean hasPermissions(Member member, String... args) {
        return false; //For once the auto complete is correct.
    }

    @Override
    protected void unload() {
        instance = null;
    }
}