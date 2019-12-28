package me.vem.jdab.utils;

import java.util.Arrays;
import java.util.Timer;
import java.util.function.Consumer;

import me.vem.jdab.struct.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class Respond {

	private static Timer timer = new Timer(true);
	
	public static void deleteMessage(long milliDelay, Message message) {
		timer.schedule(new Task(() -> message.delete().queue()), milliDelay);
	}
	
	public static void deleteMessages(TextChannel channel, long milliDelay, Message... messages) {
		timer.schedule(new Task(() -> channel.deleteMessages(Arrays.asList(messages)).queue()), milliDelay);
	}
	
	/**
	 * Synchronous bot response.
	 * @param event
	 * @param msg
	 * 
	 * @return the message object retrieved.
	 */
	public static Message sync(TextChannel channel, String msg) {
		return channel.sendMessage(msg).complete();
	}
	
	/**
	 * Synchronous bot response with printf formatting.
	 * @param channel
	 * @param format
	 * @param args
	 * @return the message object retrieved.
	 */
	public static Message syncf(TextChannel channel, String format, Object... args) {
		return sync(channel, String.format(format, args));
	}
	
	/**
	 * Asynchronous response.
	 * @param channel
	 * @param msg
	 */
	public static void async(TextChannel channel, String msg) {
		channel.sendMessage(msg).queue();
	}

	/**
     * Asynchronous response w/ success callback.
     * @param channel
     * @param msg
     */
	public static void async(TextChannel channel, String msg, Consumer<? super Message> callback) {
	    channel.sendMessage(msg).queue(callback);
	}
	
	/**
	 * Asynchronous, formatted response. Uses printf formatting.
	 * @param channel
	 * @param format
	 * @param args
	 */
	public static void asyncf(TextChannel channel, String format, Object... args) {
		async(channel, String.format(format, args));
	}
	
	/**
	 * Synchronous response. Deletes *ONLY* the bot's message.
	 * @param channel
	 * @param delay Time to delay in milliseconds.
	 * @param msg
	 */
	public static void timeout(TextChannel channel, long delay, String msg) {
		Message m = sync(channel, msg);
		if(delay > 0) deleteMessage(delay, m);
	}
	
	/**
	 * Synchronous response. Deletes the bot's message and the passed {@code userMsg} after {@code delay} milliseconds.
	 * @param channel The text channel to respond within.
	 * @param userMsg The user's message to delete
	 * @param delay The delay, in milliseconds, the bot should wait before deleting the messages.
	 * @param msg The text the bot should respond with.
	 */
	public static void timeout(TextChannel channel, Message userMsg, long delay, String msg) {
		Message m = sync(channel, msg);
		if(delay > 0)
			deleteMessages(channel, delay, m, userMsg);
	}
	
	/**
	 * Synchronous response using printf formatting. Deletes *ONLY* the bot's message.
	 * @param channel
	 * @param delay Time to delay in milliseconds.
	 * @param format
	 * @param args
	 */
	public static void timeoutf(TextChannel channel, long delay, String format, Object... args) {
		timeout(channel, delay, String.format(format, args));
	}
	
	/**
	 * Synchronous response with printf formatting. Deletes the bot's message and the passed {@code userMsg} after {@code delay} milliseconds.
	 * @param channel The text channel to respond within.
	 * @param userMsg The user's message to delete
	 * @param delay The delay, in milliseconds, the bot should wait before deleting the messages.
	 * @param format The printf format to display.
	 * @param args The objects to fill the printf formatting.
	 */
	public static void timeoutf(TextChannel channel, Message userMsg, long delay, String format, Object... args) {
		timeout(channel, userMsg, delay, String.format(format, args));
	}
	
	////////////
	/* EMBEDS */
	////////////
	
	/**
	 * Responds asynchronously with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} built from the given builder.
	 * @param channel The channel to send the message into.
	 * @param builder The builder representing the embed.
	 */
	public static void async(TextChannel channel, EmbedBuilder builder) {
		async(channel, builder.build());
	}
	
	/**
	 * Responds asynchronously with a given {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
	 * @param channel The channel to send the message into.
	 * @param embed The embed object.
	 */
	public static void async(TextChannel channel, MessageEmbed embed) {
		channel.sendMessage(embed).queue();
	}
	
	/**
	 * Responds asynchronously with a given {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
	 * Will queue with the given success callback. You're mad if I think I'm going to include a failure callback, too.
	 * @param channel
	 * @param embed
	 * @param successCallback
	 */
	public static void async(TextChannel channel, MessageEmbed embed, Consumer<? super Message> successCallback) {
        channel.sendMessage(embed).queue(successCallback);
    }
	
	/**
	 * Responds synchronously with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} built from the given builder.
	 * @param channel The channel to send the message into.
	 * @param builder The builder representing the embed.
	 * @return The message that the bot sends.
	 */
	public static Message sync(TextChannel channel, EmbedBuilder builder) {
		return sync(channel, builder.build());
	}
	
	/**
	 * Responds synchronously with the given {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
	 * @param channel The channel to send the message into.
	 * @param embed The embed object.
	 * @return The message that the bot sends.
	 */
	public static Message sync(TextChannel channel, MessageEmbed embed) {
		return channel.sendMessage(embed).complete();
	}
	
	/**
	 * Responds synchronously with the given a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} built from the given builder. Deletes the message after {@code delay} milliseconds.
	 * @param channel The channel to send the message into.
	 * @param builder The builder representing the embed.
	 * @param delay The time, in milliseconds, before the message is deleted.
	 */
	public static void timeout(TextChannel channel, EmbedBuilder builder, long delay) {
		timeout(channel, builder.build(), delay);
	}
	
	/**
	 * Responds synchronously with a given {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}. Deletes the message after {@code delay} milliseconds.
	 * @param channel The channel to send the message into.
	 * @param embed The embed object.
	 * @param delay The time, in milliseconds, before the message is deleted.
	 */
	public static void timeout(TextChannel channel, MessageEmbed embed, long delay) {
		Message m = sync(channel, embed);
		if(delay > 0) deleteMessage(delay, m);
	}
}