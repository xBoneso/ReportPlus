package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.HashMap;

public class MessageCreateEventListener implements MessageCreateListener {

    private ReportPlus main;
    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    public MessageCreateEventListener(ReportPlus main){
        this.main=main;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent e) {
        String id = Long.toString(e.getChannel().getId());
        if (id.equals(main.getMCChannelID())) {
            if (main.getConfig().getBoolean("Enabled-Modules.Chat-Sync")) {
                int cooldownTime = 5;
                if(cooldowns.containsKey(e.getMessage().getAuthor().getId())) {
                    long secondsLeft = ((cooldowns.get(e.getMessage().getAuthor().getId())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
                    if(secondsLeft>0) {
                        // Still cooling down
                        e.getChannel().sendMessage("You cannot use Chat Sync for another " + secondsLeft + " seconds.");
                        return;
                    }
                }
                if(e.getMessage().getContent().length() >= main.getConfig().getInt("Max-Chat-Sync-Characters")){
                    e.getChannel().sendMessage("Too many characters!");
                    return;
                }
                String format = ChatColor
                        .translateAlternateColorCodes('&',
                                main.getUtils().getMessagesConfig().getString("Discord-Chat-Format").replace("%user%",
                                        e.getMessage().getAuthor().getName()))
                        .replace("%message%", e.getMessage().getContent())
                        .replace("%id%", e.getMessage().getAuthor().getDiscriminator().get());
                if (e.getMessage().getUserAuthor().get().isBot()) {
                    return;
                }
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', format));
                cooldowns.put(Long.toString(e.getMessage().getAuthor().getId()), System.currentTimeMillis());
            }
        }
        else if(main.getConfig().getBoolean("Enabled-Modules.Console")) {
            if (id.equals(main.getConfig().getString("Console-Channel-ID"))){
                if (e.getMessage().getUserAuthor().get().isBot()) {
                    return;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), e.getMessage().getContent());
            }
        }
    }
}
