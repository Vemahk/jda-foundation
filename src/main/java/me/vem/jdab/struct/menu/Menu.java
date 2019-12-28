package me.vem.jdab.struct.menu;

import me.vem.jdab.listener.MenuListener;
import me.vem.jdab.utils.emoji.Emojis;
import net.dv8tion.jda.api.entities.Message;

public abstract class Menu {
	protected final Message msg;
	private int page;

	public Menu(Message msg) { this(msg, 1, true); }
	public Menu(Message msg, int page) { this(msg, page, true); }
	
	public Menu(Message msg, int page, boolean closable) {
		this.page = page;
		this.msg = msg;
		
		msg.addReaction(Emojis.LEFT_ARROW.toString()).queue();
		msg.addReaction(Emojis.RIGHT_ARROW.toString()).queue();
		if(closable)
			msg.addReaction(Emojis.XMARK.toString()).queue();

		MenuListener.getInstance().add(this);
	}
	
	/**
	 * Updates {@code msg}, for if the page number is changed.<br><br>
	 * Additional note: setPage(), nextPage(), and prevPage() all call update() at the end of their method call.
	 * Therefore, the idea is that update refreshes the {@code msg} with the newly set page.
	 */
	protected abstract void update();

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
	
	/**
	 * @param l A snowflake ID.
	 * @return true if the passed long matches the snowflake id of the message of this menu.
	 */
	public boolean matches(long l) {
		return l == msg.getIdLong();
	}
	
	public void setPage(int page) {
		this.page = page;
		update();
	}
	
	public void nextPage() {
		page++;
		update();
	}
	
	public void prevPage() {
		if(page == 1)
			return;
		page--;
		update();
	}
	
	public void destroy() {
		//The reason for the lambdas it to quiet errors of it not existing... In case third parties delete it before it can delete itself.
		msg.delete().queue((success) -> {}, (failure) -> {});
	}
}
