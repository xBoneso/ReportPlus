package me.xbones.reportplus.bungee;

import net.md_5.bungee.api.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

public class LogAppender extends java.util.logging.Handler {

     private me.xbones.reportplus.bungee.ReportPlus main;

     public LogAppender(ReportPlus main) {

         this.main = main;
     }

     @Override
     public void flush() {
     }

     @Override
     public void close() throws SecurityException {
     }

     @Override
     public void publish(LogRecord record) {



         String message = record.getMessage();

         SimpleDateFormat formatter;
         formatter = new SimpleDateFormat("HH:mm:ss");
         message = "[" + formatter.format(new Date(record.getMillis())) + " " + record.getLevel().toString() + "] " + message;
         if (main.getBot() != null)
             main.getBot().getTextChannelById((String)main.getValue("Console-Channel-ID", "[INSERT ID HERE]")).get().sendMessage(ChatColor.stripColor(message));


     }

 }
