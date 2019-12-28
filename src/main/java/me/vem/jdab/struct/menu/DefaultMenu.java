package me.vem.jdab.struct.menu;

import net.dv8tion.jda.api.entities.Message;

public abstract class DefaultMenu extends Menu{

	public DefaultMenu(Message msg) { super(msg); }
	public DefaultMenu(Message msg, int page) { super(msg, page); }
	public DefaultMenu(Message msg, int page, boolean closable) { super(msg, page, closable); }
	
	@Override
	protected void update() {
		msg.editMessage(getResponse(getPage())).queue();
	}

	public abstract String getResponse(int page);
}
