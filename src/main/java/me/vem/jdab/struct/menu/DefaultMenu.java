package me.vem.jdab.struct.menu;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public abstract class DefaultMenu extends Menu{

	public DefaultMenu(MessageChannel channel) { super(channel); }
	public DefaultMenu(MessageChannel channel, int page) { super(channel, page); }
	public DefaultMenu(MessageChannel channel, int page, boolean closable) { super(channel, page, closable); }
	
	@Override
	protected void update(Message msg) {
		msg.editMessage(getResponse(getPage())).queue();
	}

	public abstract String getResponse(int page);
}
