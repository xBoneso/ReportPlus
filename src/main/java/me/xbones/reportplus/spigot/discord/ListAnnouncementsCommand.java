package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class ListAnnouncementsCommand implements MessageCreateListener {
	
	ReportPlus main;
	public ListAnnouncementsCommand(ReportPlus main) {
		this.main = main;
	}

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().equalsIgnoreCase(main.getCmdPrefix() + "listannouncements")) {

            if (!event.getMessage().getAuthor().isServerAdmin()) {
                event.getChannel().sendMessage("You are not allowed to use this command!");
                return;
            }
            event.getChannel().sendMessage("```Announcements:```");
            int count = 1;
            for (String s : main.getMessages()) {
                event.getChannel().sendMessage("```" + count + " - " + s + "```");
                count++;
            }
        }
    }
  }
