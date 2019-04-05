package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class HelpCommand implements MessageCreateListener {

    private ReportPlus main;

    public HelpCommand(ReportPlus main) {
        this.main = main;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().equalsIgnoreCase(main.getCmdPrefix() + "help")) {

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Hello, " + event.getMessage().getAuthor().getName() +"! Welcome to the server " + main.getConfig().getString("Server-Name"))

                    .setDescription("Here are the commands with description!")
                    .setColor(new Color(16711680))
                    .setTimestamp(Instant.now())
                    .setFooter("Report+ by xBones! Hope you like it!", "https://www.spigotmc.org/data/resource_icons/50/50455.jpg?1512679915")
                    .setThumbnail(main.getConfig().getString("Server-Icon-Link"))
                    .setImage(main.getConfig().getString("Server-Icon-Link"))
                    .setAuthor("Report+ by xBones", "https://www.spigotmc.org/resources/report-gui-%E2%98%86-discord-%E2%98%86-customizable-%E2%98%86-titles-%E2%98%86-discord-announcements-%E2%98%86-and-more.50455/", main.getConfig().getString("Server-Icon-Link"))
                    .addField("**help**", "The command that shows all...", false)
                    .addField("**reload**", "Reload the config", false)
                    .addField("**addannounce**", "Add announcement!", false)
                    .addField("**delannouncement**", "Delete an announcement!", false)
                    .addField("**listannouncements**", "List announcements!", false)
                    .addField("**closereport**", "Closes the report with the id specified and sends specified message to report owner!", false);

            if (main.getConfig().getConfigurationSection("TXTCmds") != null) {
                Set<String> Cmds = new HashSet<>();
                Cmds = main.getConfig().getConfigurationSection("TXTCmds").getKeys(false);
                for(String cmd : Cmds){
                    builder.addField("**" + cmd + "**", main.getConfig().getString("TXTCmds." + cmd + ".description"), false);
                }
            }

            if (main.getConfig().getConfigurationSection("Cmds") != null) {
                Set<String> Cmds;
                Cmds = main.getConfig().getConfigurationSection("Cmds").getKeys(false);
                for (String cmd : Cmds) {
                    builder.addField("**" + cmd + "**", main.getConfig().getString("Cmds." + cmd + ".description"), false);

                }
            }
        event.getChannel().sendMessage(builder);
        }
    }
}
