package me.vem.jdab.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import me.vem.jdab.struct.Task;
import me.vem.jdab.struct.menu.Menu;
import me.vem.jdab.utils.emoji.Emoji;
import me.vem.jdab.utils.emoji.Emojis;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class MenuListener implements EventListener{

	private static MenuListener instance;
	public static MenuListener getInstance() {
		if(instance == null)
			instance = new MenuListener();
		return instance;
	}

	public static void unload() {
		if(instance == null)
			return;
		
		instance.timeout.cancel();
		
		for(Menu menu : instance.openMenues.values())
            menu.destroy();
		
		instance.openMenues.clear();
		
		instance = null;
	}
	
	private final Map<Long, Menu> openMenues;
	private final Timer timeout;
	
	private MenuListener() {
		timeout = new Timer("Menu Timer");
		openMenues = new HashMap<>();
	}
	
	/**
	 * Schedules the destruction and removal of the given menu after '{@code delay}' seconds.
	 * @param menu
	 * @param delay
	 */
	public void timeout(Menu menu, int delay) {
		timeout.schedule(new Task(() -> {
            if(!menu.isInitialized()) 
                return;
            
            MenuListener.this.remove(menu);
            menu.destroy();
		}), delay);
	}
	
	public Menu add(Menu m) {
	    if(!m.isInitialized())
	        throw new IllegalArgumentException("Cannot listen for an uninitialized menu!");
	    
		return openMenues.put(m.getMessageId(), m);
	}
	
	public void remove(Menu m) {
        if(!m.isInitialized())
            throw new IllegalArgumentException("Cannot remove menu! Menu is uninitialized, and thus I cannot retrieve the message's snowflake-id.");
	    
		openMenues.remove(m.getMessageId());
	}
	
	@Override
	public void onEvent(GenericEvent event) {
		if(event instanceof MessageReactionAddEvent)
			addReaction((MessageReactionAddEvent)event);
		else if(event instanceof MessageReactionRemoveEvent)
			remReaction((MessageReactionRemoveEvent)event);
	}
	
	private void addReaction(MessageReactionAddEvent event) {
		if(event.getUser().equals(event.getJDA().getSelfUser()))
			return;
		
		Menu menu = openMenues.get(event.getMessageIdLong());
		if(menu == null)
		    return;
		
		Emoji reaction = new Emoji(event.getReactionEmote());
		
		if(reaction.equals(Emojis.LEFT_ARROW))
			menu.prevPage();
		else if(reaction.equals(Emojis.RIGHT_ARROW))
		    menu.nextPage();
		else if(reaction.equals(Emojis.XMARK)){
		    remove(menu);
            menu.destroy();
		}
	}
	
	private void remReaction(MessageReactionRemoveEvent event) {
		if(event.getUser().equals(event.getJDA().getSelfUser()))
			return;
        
        Menu menu = openMenues.get(event.getMessageIdLong());
        if(menu == null)
            return;
		
		Emoji reaction = new Emoji(event.getReactionEmote());
		
		if(reaction.equals(Emojis.LEFT_ARROW))
			menu.prevPage();
		else if(reaction.equals(Emojis.RIGHT_ARROW))
		    menu.nextPage();
	}
}