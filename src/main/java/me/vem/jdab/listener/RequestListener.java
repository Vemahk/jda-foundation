package me.vem.jdab.listener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.vem.jdab.utils.Request;
import me.vem.jdab.utils.emoji.Emoji;
import me.vem.jdab.utils.emoji.Emojis;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class RequestListener implements EventListener{

	private static RequestListener instance;
	public static RequestListener getInstance() {
		if(instance == null)
			instance = new RequestListener();
		return instance;
	}
	
	public static void unload() {
		Iterator<Request> iter = getInstance().openRequests.iterator();
		while(iter.hasNext()) {
			iter.next().denied();
			iter.remove();
		}
		
		instance = null;
	}
	
	public static void add(Request c) {
		getInstance().openRequests.add(c);
	}
	
	public static void remove(Request c) {
		getInstance().openRequests.remove(c);
	}
	
	List<Request> openRequests;
	
	private RequestListener() {
		openRequests = new LinkedList<>();
	}

	@Override
	public void onEvent(GenericEvent event) {
		if(event instanceof MessageReactionAddEvent)
			reactionAdded((MessageReactionAddEvent)event);
	}
	
	private void reactionAdded(MessageReactionAddEvent event) {
		if(event.getUser().equals(event.getJDA().getSelfUser()))
			return;
		
		Emoji reaction = new Emoji(event.getReactionEmote());
		
		if(!(reaction.equals(Emojis.CHECK) || reaction.equals(Emojis.XMARK)))
			return;
		
		Request request = null;
		
		for(Request requ : openRequests)
			if(requ.getMessageID() == event.getMessageIdLong()) {
				request = requ;
				break;
			}
		
		if(request == null)
			return;
		
		if(!event.getUser().equals(request.getCaller()))
			return;
		
		if(reaction.equals(Emojis.CHECK))
			request.confirmed();
		else request.denied();
		
		openRequests.remove(request);
	}
}
