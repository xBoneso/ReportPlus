package me.xbones.reportplus.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.javacord.api.entity.channel.TextChannel;

public class PlayerMessageListener implements Listener {

    private ReportPlus main;

    public PlayerMessageListener(ReportPlus main){
        this.main=main;
    }

    @EventHandler
    public void onMessageCreated(ChatEvent e){
        String message = e.getMessage();
        if(e.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) e.getSender();

            if ((boolean) main.getValue("Enabled-Modules.Chat-Sync", false)) {
                TextChannel channel = main.getBot().getTextChannelById((String) main.getValue("Chat-Sync-Channel-ID", "[INSERT ID HERE]")).get();
                String format = (String) main.getValue("Chat-Sync-Format", "%player% -> %message%");
                String newMessage = format.replace("%player%", player.getName()).replace("%message%", message);
                channel.sendMessage(newMessage);
            }
        }
    }
}
