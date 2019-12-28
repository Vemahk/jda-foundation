package me.vem.jdab.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.reflect.TypeToken;

import me.vem.jdab.DiscordBot;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class Utilities {

	private static Pattern memberMentionPattern = Pattern.compile("<@\\!?(\\d+)>");
	private static Pattern roleMentionPattern = Pattern.compile("<@&(\\d+)>");
	private static Pattern channelMentionPattern = Pattern.compile("<#(\\d+)>");
	private static Pattern emotePattern = Pattern.compile("<a?:(.+?):(\\d+)>");
	
	/**
	 * For more information, see: {@link https://discordapp.com/developers/docs/reference#message-formatting}
	 * @param mention (e.g. <@##############>)
	 * @return null if the mention is not in the correct format.<br>
	 * null if the user is not found.<br>
	 * The user if it is a valid mention.
	 */
	public static User getUserFromMention(String mention) {
		Matcher matcher = memberMentionPattern.matcher(mention);
		if(!matcher.matches())
			return null;
		
		return DiscordBot.getInstance().getJDA().getUserById(matcher.group(1));
	}
	
	/**
	 * For more information, see: {@link https://discordapp.com/developers/docs/reference#message-formatting}
	 * @param guild The guild associated with the user's membership.
	 * @param mention (e.g. <@000000000000000> or <@!000000000000000>)
	 * @return null if the mention is not in the correct format.<br>
	 * null if the user is not found.<br>
	 * The member if it is a valid mention and they are apart of the given guild.
	 */
	public static Member getMemberFromMention(Guild guild, String mention) {
		User u = getUserFromMention(mention);
		if(u == null) return null;
		return guild.getMember(u);
	}
	
	/**
	 * For more information, see: {@link https://discordapp.com/developers/docs/reference#message-formatting}
	 * @param guild The guild associated with this role.
	 * @param mention (e.g. <@&000000000000000>)
	 * @return null if the mention is not in the correct format.<br>
	 * null if the role is not found.<br>
	 * The role if the mention is valid and the role is apart of this guild.
	 */
	public static Role getRoleFromMention(Guild guild, String mention) {
		Matcher matcher = roleMentionPattern.matcher(mention);
		if(!matcher.matches())
			return null;
		
		return guild.getRoleById(matcher.group(1));
	}
	
	/**
	 * For more information, see: {@link https://discordapp.com/developers/docs/reference#message-formatting}
	 * @param guild The guild associated with the text channel.
	 * @param mention (e.g. <#000000000000000>)
	 * @return null if the mention is not in the correct format. <br>
	 * null if the text channel is not found. <br>
	 * The text channel if the mention is valid and it exists within the provided guild.
	 */
	public static TextChannel getTextChannelFromMention(Guild guild, String mention) {
		Matcher matcher = channelMentionPattern.matcher(mention);
		if(!matcher.matches())
			return null;
		
		return guild.getTextChannelById(matcher.group(1));
	}
	
	/**
	 * For more information, see: {@link https://discordapp.com/developers/docs/reference#message-formatting} <br>
	 * Note: Discord utilizes Unicode 9.0 for a lot of its in-built emoji's. Because of this,
	 * some default emoji's will not be read in this mention type, but rather in their UTF-32 form.
	 * This causes some fun times trying to read them.
	 * @param guild The guild the mention took place in.
	 * @param mention The mention (e.g. <:emote_name:000000000000000>)
	 * @return null if the mention is not in the correct format. <br>
	 * null if the emote is not valid.<br>
	 * The emote represented by the mention if the mention is valid and it exists within the provided guild.
	 */
	public static Emote getEmoteFromMention(Guild guild, String mention) {
		Matcher matcher = emotePattern.matcher(mention);
		if(!matcher.matches())
			return null;
		
		return guild.getEmoteById(matcher.group(2));
	}
	
	public static  String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        
        boolean display_day = days > 0;
        boolean display_hour = hours > 0;
        boolean display_minute = minutes > 0;
        boolean display_second = seconds > 0;
        
        boolean plural_day = days > 1;
        boolean plural_hour = hours > 1;
        boolean plural_minute = minutes > 1;
        boolean plural_second = seconds > 1;
        
        StringBuilder str = new StringBuilder();
        
        if(display_day){
            str.append(days).append(" day");
            if(plural_day) 
                str.append('s');
        }
        
        if(display_hour) {
            if(display_day)
                if(!(display_minute || display_second))
                    str.append(" and ");
                else str.append(", ");
            str.append(hours).append(" hour");
            if(plural_hour)
                str.append('s');
        }
        
        if(display_minute) {
            if(display_day || display_hour) {
                if(display_second || display_day && display_hour)
                    str.append(',');
                
                str.append(' ');
                
                if(!display_second)
                    str.append("and ");
            }
            
            str.append(minutes).append(" minute");
            if(plural_minute)
                str.append('s');
        }
        
        if(display_second) {
            int count_before = countTrues(display_day, display_hour, display_minute);
            
            if(count_before > 0) {
                if(count_before != 1)
                    str.append(',');
                str.append(" and ");
            }
            
            str.append(seconds).append(" second");
            if(plural_second)
                str.append('s');
        }
        
        return str.toString();
    }
	
	private static int countTrues(boolean... bools) {
        int out = 0;
        for(boolean b : bools)
            if(b) out++;
        return out;
    }
	
	public static <T> Type typeOf() {
	    return new TypeToken<T>(){}.getType();
	}
	
	public static boolean saveToJSONFile(String filename, Object toJson) {
		try {
			PrintWriter out = ExtFileManager.getConfigOutput(filename);
			out.print(ExtFileManager.getGsonPretty().toJson(toJson));
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
