package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class AddAnnouncementCommand implements MessageCreateListener{
	
	private ReportPlus main;

	public AddAnnouncementCommand(ReportPlus main) {
		this.main = main;
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().toLowerCase().startsWith(main.getCmdPrefix() + "addannounce")) {

            if (!event.getMessage().getAuthor().isServerAdmin()) {
                event.getChannel().sendMessage("You are not allowed to use this command!");
                return;
            }

            String[] args = event.getMessage().getContent().split(" ");
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++){
                sb.append(args[i]).append(" ");
            }

            String announcement = sb.toString().trim();
            main.getMessages().add(announcement);
            main.getConfig().set("Announcements", main.getMessages());
            main.saveConfig();
            main.reloadConfig();
            event.getChannel().sendMessage("```Announcement " + announcement + " has been added!```");

        }
    }

  }
