package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.ReportPlus;
import org.bukkit.Bukkit;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class CustomCMD implements MessageCreateListener {

    private ReportPlus main;
private String CMD;
    public CustomCMD(ReportPlus main, String CMD) {
        this.main = main;
        this.CMD=CMD;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().equalsIgnoreCase(main.getCmdPrefix() + CMD)) {

            event.getChannel().sendMessage(main.getConfig().getString("Cmds." + CMD + ".say"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    main.getConfig().getString("Cmds." + CMD + ".targetcmd"));

        }
    }
}
