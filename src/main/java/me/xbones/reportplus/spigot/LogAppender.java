package me.xbones.reportplus.spigot;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogAppender extends AbstractAppender {

    private ReportPlus main;

    public LogAppender(ReportPlus main) {
        super("MyLogAppender", null, null);
        this.main=main;
        start();
    }

    @Override
    public void append(LogEvent event) {

        String message = event.getMessage().getFormattedMessage();

        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("HH:mm:ss");
        message = "[" +formatter.format(new Date(System.currentTimeMillis())) + " " + event.getLevel().toString() + "] " + message;
        if(main.getBot() != null)
        main.getBot().getTextChannelById(main.getConfig().getString("Console-Channel-ID")).get().sendMessage(ChatColor.stripColor(message));
    }

}
