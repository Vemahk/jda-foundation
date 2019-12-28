package me.vem.jdab.listener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
		Iterator<Menu> iter = instance.openMenues.iterator();
		while(iter.hasNext()) {
			iter.next().destroy();
			iter.remove();
		}
		
		instance = null;
	}
	
	private final List<Menu> openMenues;
	private final Timer timeout;
	
	private MenuListener() {
		timeout = new Timer("Menu Timer");
		openMenues = new LinkedList<>();
	}
	
	/**
	 * Schedules the destruction and removal of the given menu after '{@code delay}' seconds.
	 * @param menu
	 * @param delay
	 */
	public void timeout(Menu menu, int delay) {
		timeout.schedule(new Task(() -> {
			menu.destroy();
			MenuListener.this.openMenues.remove(menu);
		}), delay);
	}
	
	public void add(Menu m) {
		if(!openMenues.contains(m))
			openMenues.add(m);
	}
	
	public void remove(Menu m) {
		openMenues.remove(m);
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
		
		Emoji reaction = new Emoji(event.getReactionEmote());
		
		if(reaction.equals(Emojis.LEFT_ARROW) || reaction.equals(Emojis.RIGHT_ARROW)){
			for(Menu menu : openMenues)
				if(menu.matches(event.getMessageIdLong()))
					if(reaction.equals(Emojis.LEFT_ARROW))
						menu.prevPage();
					else menu.nextPage();
		}else if(reaction.equals(Emojis.XMARK)){
			Iterator<Menu> iter = openMenues.iterator();
			while(iter.hasNext()) {
				Menu next = iter.next();
				if(next.matches(event.getMessageIdLong())) {
					next.destroy();
					iter.remove();
					break;
				}
			}
		}
	}
	
	private void remReaction(MessageReactionRemoveEvent event) {
		if(event.getUser().equals(event.getJDA().getSelfUser()))
			return;
		
		Emoji reaction = new Emoji(event.getReactionEmote());
		
		//Look, ma! No brackets!
		if(reaction.equals(Emojis.LEFT_ARROW) || reaction.equals(Emojis.RIGHT_ARROW))
			for(Menu menu : openMenues)
				if(menu.matches(event.getMessageIdLong()))
					if(reaction.equals(Emojis.LEFT_ARROW))
						menu.prevPage();
					else menu.nextPage();
	}
}