package me.vem.jdab.utils;

import java.awt.Color;

import me.vem.jdab.listener.RequestListener;
import me.vem.jdab.utils.emoji.Emojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class Request {

	private User caller;
	private Message msg;
	private Runnable success, failure;
	
	public Request(User user, TextChannel channel, String desc, Runnable success, Runnable failure) {
		caller = user;
		this.success = success;
		this.failure = failure;
		
		msg = Respond.sync(channel, new EmbedBuilder().setTitle("Are you sure?").setDescription(desc).setColor(Color.CYAN).build());
		msg.addReaction(Emojis.CHECK.toString()).queue();
		msg.addReaction(Emojis.XMARK.toString()).queue();
		RequestListener.add(this);
	}
	
	public User getCaller() {
		return caller;
	}
	
	public long getMessageID() {
		return msg.getIdLong();
	}

	public void confirmed() {
		success.run();
		msg.delete().queue();
	}

	public void denied() {
		failure.run();
		msg.delete().queue();
	}
}
