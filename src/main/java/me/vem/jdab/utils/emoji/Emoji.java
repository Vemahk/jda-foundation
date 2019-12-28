package me.vem.jdab.utils.emoji;

import org.jetbrains.annotations.NotNull;

import com.vdurmont.emoji.EmojiManager;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;

public class Emoji{
	private Emote emote;
	private String utf;
	
	/**
	 * @param e The custom emote
	 * @throws IllegalArgumentException if passed a null emote.
	 */
	public Emoji(@NotNull Emote e) {
		if(e == null)
			throw new IllegalArgumentException("Passed Emote is null.");
		emote = e;
	}
	
	/**
	 * @param utf The UTF-32 representation of an emoji
	 * @throws IllegalArgumentException if the passed string does not represent a UTF-32 Emoji.
	 */
	public Emoji(String utf) {
		if(!EmojiManager.isEmoji(utf))
			throw new IllegalArgumentException("Passed string must be a UTF-32 Emoji");
		
		this.utf = utf;
	}
	
	public Emoji(ReactionEmote re) {
		if(re.isEmote())
			emote = re.getEmote();
		else utf = re.getName();
	}
	
	public boolean isEmote() {
		return emote != null;
	}
	
	public Emote getEmote() {
		return emote;
	}
	
	@Override
	public String toString() {
		if(emote != null)
			return emote.getAsMention();
		return utf;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emote == null) ? 0 : emote.hashCode());
		result = prime * result + ((utf == null) ? 0 : utf.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Emoji other = (Emoji) obj;
		if (emote == null) {
			if (other.emote != null)
				return false;
		} else if (!emote.equals(other.emote))
			return false;
		if (utf == null) {
			if (other.utf != null)
				return false;
		} else if (!utf.equals(other.utf))
			return false;
		return true;
	}
}