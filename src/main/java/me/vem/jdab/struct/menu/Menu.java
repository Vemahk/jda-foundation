package me.vem.jdab.struct.menu;

import me.vem.jdab.listener.MenuListener;
import me.vem.jdab.utils.emoji.Emojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class Menu {
	private Message msg;
	private int page;

	public Menu(MessageChannel channel) { this(channel, 1, true); }
	public Menu(MessageChannel channel, int page) { this(channel, page, true); }
	
	public Menu(MessageChannel channel, int page, boolean closable) {
		this.page = page;
		
		MessageEmbed loading = new EmbedBuilder().setTitle("Loading Menu...").build();
		Message message = new MessageBuilder().setEmbed(loading).build();
		channel.sendMessage(message).queue(msg -> {
	        this.msg = msg;
	        
	        update(msg);
	        msg.addReaction(Emojis.LEFT_ARROW.toString()).queue();
	        msg.addReaction(Emojis.RIGHT_ARROW.toString()).queue();
	        if(closable)
	            msg.addReaction(Emojis.XMARK.toString()).queue();
	        
	        MenuListener.getInstance().add(this);
		});
	}
	
	/**
	 * Updates {@code msg}, for if the page number is changed.<br><br>
	 * Additional note: setPage(), nextPage(), and prevPage() all call update() at the end of their method call.
	 * Therefore, the idea is that update refreshes the {@code msg} with the newly set page.
	 */
	protected abstract void update(Message msg);

	private boolean isScheduled = false;
	/**
	 * @param delay The number of seconds until this menu is to be destroyed.
	 * @return true if this menu object is not already scheduled to be removed. False otherwise.
	 */
	public boolean setTimeout(int delay) {
		if(isScheduled)
			return false;
		
		MenuListener.getInstance().timeout(this, delay * 1000);
		
		isScheduled = true;
		return true;
	}
	
	public int getPage() { return page; }
	public boolean isInitialized() { return msg != null; }
	
	public long getMessageId() {
        if(msg == null)
            throw new IllegalStateException("Menu has not yet been initialized or is no longer valid.");
        
	    return msg.getIdLong();
	}
	
	public void setPage(int page) {
        if(msg == null)
            throw new IllegalStateException("Menu has not yet been initialized or is no longer valid.");
	    
		this.page = page;
		update(msg);
	}
	
	public void nextPage() {
	    if(msg == null)
            throw new IllegalStateException("Menu has not yet been initialized or is no longer valid.");
        
		page++;
		update(msg);
	}
	
	public void prevPage() {
	    if(msg == null)
            throw new IllegalStateException("Menu has not yet been initialized or is no longer valid.");
        
		if(page == 1)
			return;
		
		page--;
		update(msg);
	}
	
	public void destroy() {
	    if(msg == null)
            throw new IllegalStateException("Menu has not yet been initialized or is no longer valid.");
        
		msg.delete().queue();
		msg = null;
	}
}
