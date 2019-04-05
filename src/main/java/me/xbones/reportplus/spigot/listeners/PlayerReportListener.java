package me.xbones.reportplus.spigot.listeners;

import me.xbones.reportplus.spigot.ReportPlus;
import me.xbones.reportplus.spigot.events.PlayerReportEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerReportListener implements Listener {

    private ReportPlus main;

    public PlayerReportListener(ReportPlus main)
    {
        this.main=main;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onReport(PlayerReportEvent e)
    {
        main.getSqlManager().refresh();
    }
}
