package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DelAnnouncementCommand implements MessageCreateListener {
	
	ReportPlus main;
	public DelAnnouncementCommand(ReportPlus main) {
		this.main = main;
	}

	 @Override
     public void onMessageCreate(MessageCreateEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(main.getCmdPrefix() + "delannouncement")) {

			 if (!event.getMessage().getAuthor().isServerAdmin()) {
				 event.getChannel().sendMessage("You are not allowed to use this command!");
				 return;
			 }
			 String[] args = event.getMessage().getContent().split(" ");


			 if(args.length == 0) {
				 event.getChannel().sendMessage("```Please enter id!```");
				 return;
			 }

			 int id = Integer.valueOf(args[1]);
			 event.getChannel().sendMessage("```Announcement " + main.getMessages().get(id - 1) + " has been removed!```");
			 main.getMessages().remove(id - 1);
			 main.getConfig().set("Announcements", main.getMessages());
			 main.saveConfig();
			 main.reloadConfig();
		 }
	 }

  }
