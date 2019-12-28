package me.vem.jdab.struct;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import me.vem.jdab.utils.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class MessagePurge implements Collection<Message>, Iterable<Message>{
	
    //Bots can only delete messages that were sent within the last two weeks.
	private static final int msptw = 1000 * 60 * 60 * 24 * 7 * 2; //msptw -> milliseconds per two weeks.
	
	/**
	 * Deletes all messages of the given channel within 2 weeks.
	 * @param channel
	 */
	public static void purge(TextChannel channel) {
		if(channel == null) {
			Logger.warn("Purge fail");
			return;
		}
		
		MessagePurge purge = new MessagePurge(channel);
		for(Message msg : channel.getIterableHistory().cache(false))
			purge.add(msg);
		purge.clear();
	}
	
	/**
	 * Deletes all messages in channel that pass the test of a predicate within 2 weeks.
	 * @param channel
	 * @param predicate
	 */
	public static void purge(TextChannel channel, Predicate<Message> predicate) {
		if(channel == null) {
			Logger.warn("Purge fail");
			return;
		}
		
		if (predicate == null) {
			purge(channel);
			return;
		}
		
		MessagePurge purge = new MessagePurge(channel);
		for(Message msg : channel.getIterableHistory().cache(false))
			if(predicate.test(msg))
				purge.add(msg);
		purge.clear();
	}
	
	/**
	 * Deletes the last 'n' messages within the given channel, assuming they were sent within the last 2 weeks.
	 * @param channel
	 * @param n
	 */
	public static void purge(TextChannel channel, long n) {
		if(channel == null) {
			Logger.warn("Purge fail");
			return;
		}
		
		if (n <= 0) {
			purge(channel);
			return;
		}
		
		MessagePurge purge = new MessagePurge(channel);
		for(Message msg : channel.getIterableHistory().cache(false)) {
			purge.add(msg);
			if(--n <= 0)
				break;
		}
		purge.clear();
	}
	
	/**
	 * Checks the last 'n' messages of a given channel and deletes them if they pass a predicate, all assuming that the message is not more than 2 weeks old.
	 * @param channel
	 * @param n
	 * @param predicate
	 */
	public static void purge(TextChannel channel, long n, Predicate<Message> predicate) {
		if(channel == null) {
			Logger.warn("Purge fail");
			return;
		}
		
		if (n <= 0) {
			purge(channel, predicate);
			return;
		}
		
		if(predicate == null) {
			purge(channel, n);
			return;
		}
		
		MessagePurge purge = new MessagePurge(channel);
		for(Message msg : channel.getIterableHistory().cache(false)) {
			if(predicate.test(msg))
				purge.add(msg);
			if(--n <= 0)
				break;
		}
		purge.clear();
	}
	
	private Message[] list;
	private int len;
	
	private TextChannel linkedChannel;
	
	public MessagePurge(TextChannel tc){
		list = new Message[100];
		len = 0;
		linkedChannel = tc;
	}
	
	@Override
	public boolean add(Message e) {
		long dt = System.currentTimeMillis() - e.getTimeCreated().toEpochSecond() * 1000;
		if(dt - msptw >= -1000) //If it's two weeks or older (with a grace of a second, for paranoia reasons).
			return false;
		
		if(len >= 100) 
			clear();
		list[len++] = e;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Message> c) {
		for(Message m : c)
			add(m);
		return true;
	}

	@Override
	public void clear() {
		if(len == 1) linkedChannel.deleteMessageById(list[0].getIdLong()).queue();
		else if(len > 1)  linkedChannel.deleteMessages(this).queue();
		
		len = 0;
	}

	@Override
	public boolean contains(Object o) {
		if(!(o instanceof Message))
			return false;
		
		for(int i=0;i<len;i++)
			if(list[i] == o)
				return true;
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object m : c)
			if(!contains(m))
				return false;
		return true;
	}

	@Override public boolean isEmpty() { return len == 0; }
	@Override public int size() { return len; }

	@Override
	public Iterator<Message> iterator() {
		return new Iterator<Message>() {
			private int next = 0;
			@Override public boolean hasNext() { return next < len; }
			@Override public Message next() { return list[next++]; }
		};
	}

	@Override public Object[] toArray() { return list; } 
	
	/*
	 * Unwritten as this is a purge list. Anything added is meant to be destroyed.
	 */
	@Override public boolean remove(Object o) { return false; }
	@Override public boolean removeAll(Collection<?> c) { return false; }
	@Override public boolean retainAll(Collection<?> c) { return false; }

	/*
	 * Unimplemented because this is not a generic list.
	 */
	@Override public <T> T[] toArray(T[] a) { return null; }
}