package me.vem.jdab.struct.menu;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class EmbedMenu extends Menu{

	public EmbedMenu(MessageChannel channel) { super(channel); }
	public EmbedMenu(MessageChannel channel, int page) { super(channel, page); }
	public EmbedMenu(MessageChannel channel, int page, boolean closable) { super(channel, page, closable); }
	
	@Override
	protected void update(Message msg) {
		msg.editMessage(getEmbed(getPage())).queue();
	}
	
	public abstract MessageEmbed getEmbed(int page);
}
