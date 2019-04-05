package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class ReloadCommand implements MessageCreateListener {
	
	private ReportPlus main;
	public ReloadCommand(ReportPlus main) {
		this.main = main;
	}
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().equalsIgnoreCase(main.getCmdPrefix() + "reload")) {

            if (!event.getMessage().getAuthor().isServerAdmin()) {
                event.getChannel().sendMessage("You are not allowed to use this command!");
                return;
            }

            main.reloadConfig();
            event.getChannel().sendMessage("Successfully reloaded!");
        }
    }
  }
