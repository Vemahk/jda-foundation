package me.vem.jdab;

import javax.security.auth.login.LoginException;

import me.vem.jdab.cmd.Command;
import me.vem.jdab.cmd.Configurable;
import me.vem.jdab.cmd.Help;
import me.vem.jdab.cmd.Prefix;
import me.vem.jdab.cmd.Uptime;
import me.vem.jdab.listener.CommandListener;
import me.vem.jdab.listener.MenuListener;
import me.vem.jdab.listener.RequestListener;
import me.vem.jdab.utils.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

/**
 * The bot object that will open the JDA instance upon initialization.
 * @author Vemahk
 */
public class DiscordBot {

	private static DiscordBot instance;
	public static DiscordBot getInstance() { return instance; }
	public static DiscordBot initialize(String token) {
	    if(instance != null)
	        throw new IllegalStateException("DiscordBot has already been initialized! Cannot reinitialize!");
	    
		if(token == null || token.isEmpty()) 
			throw new RuntimeException("Bot Token was null or empty. DiscordBot failed to load.");
		
		return new DiscordBot(token);
	}
	
	private JDA jda;
	
	private DiscordBot(String token) {
		try {
			//Look at this trash. Don't be like me.
			(jda = new JDABuilder(token)
					.addEventListeners(CommandListener.getInstance())
					.build().awaitReady())
					.setAutoReconnect(true);
		}catch(LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
			return;
		}
		
		instance = this;

		jda.addEventListener(MenuListener.getInstance());
		jda.addEventListener(RequestListener.getInstance());
		
		Help.initialize();
		Prefix.initialize();
		Uptime.initialize();
	}
	
	public void addEventListener(Object listener) {
		jda.addEventListener(listener);
	}
	
	public void removeEventListener(Object listener) {
		jda.removeEventListener(listener);
	}
	
	public JDA getJDA() {
		return jda;
	}
	
	private Runnable preShutdown, postShutdown;
	public void setPreShutdown(Runnable r) { preShutdown = r; }
	public void setPostShutdown(Runnable r) { postShutdown = r; }
	
	/**
	 * Shuts down the JDA instance safely.
	 * 
	 * Closes all related threads so that the program will shutdown safely.
	 */
	public void shutdown() {
		if(preShutdown != null)
			preShutdown.run();
		
		Logger.info("Shutting down...");
		
		//Call all registered commands' unload function.
		Command.unloadAll();
		
		MenuListener.unload();
		RequestListener.unload();
		
		//Save all configurable event listeners
		for(Object o : jda.getRegisteredListeners())
			if(o instanceof Configurable)
				((Configurable)o).save();
		
		//Safely shutdown the JDA
		jda.shutdown();
		
		//Set instance to null for potential reinitialization.
		instance = null;
		
		if(postShutdown != null)
			postShutdown.run();
	}
}