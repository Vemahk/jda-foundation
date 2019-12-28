package me.vem.jdab.struct.menu;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class EmbedMenu extends Menu{

	public EmbedMenu(Message msg) { super(msg); }
	public EmbedMenu(Message msg, int page) { super(msg, page); }
	public EmbedMenu(Message msg, int page, boolean closable) { super(msg, page, closable); }
	
	@Override
	protected void update() {
		msg.editMessage(getEmbed(getPage())).queue();
	}
	
	public abstract MessageEmbed getEmbed(int page);
}
