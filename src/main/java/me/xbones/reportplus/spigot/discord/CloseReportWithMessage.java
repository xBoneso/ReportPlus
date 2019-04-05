package me.xbones.reportplus.spigot.discord;

import me.xbones.reportplus.spigot.Report;
import me.xbones.reportplus.spigot.ReportPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;
import java.util.List;

public class CloseReportWithMessage implements MessageCreateListener{

	private ReportPlus main;

	public CloseReportWithMessage(ReportPlus main) {
		this.main = main;
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().toLowerCase().startsWith(main.getCmdPrefix() + "closereport")) {

            if (!event.getMessage().getAuthor().isServerAdmin()) {
                event.getChannel().sendMessage("You are not allowed to use this command!");
                return;
            }

            String[] args = event.getMessage().getContent().split(" ");

            if(args.length < 3){
                event.getChannel().sendMessage("Please enter ID and message.");
                return;
            }
            int id = Integer.parseInt(args[1]);

            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++){
                sb.append(args[i]).append(" ");
            }

            Report selectedReport = null;
            for(Report r : main.getReportsList()){
                if(r.getReportId() == id){
                    selectedReport = r;
                }
            }

            if(selectedReport == null){
                event.getChannel().sendMessage("Report not found. Please make sure the id is correct.");
                return;
            }
            main.getInventoryClickListener().CloseReport(event.getMessage().getAuthor().getDiscriminatedName(), selectedReport, true);

            String Message = sb.toString().trim();
            event.getChannel().sendMessage("```Report #" + id + " closed with message: " + Message + "```");

            Player reportOwner = Bukkit.getPlayer(selectedReport.getReporter());
            if(reportOwner != null){
                reportOwner.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getUtils().getMessagesConfig().getString("Message-Notification-Format").replace("%sender%", event.getMessage().getAuthor().getDiscriminatedName()).replace("%message%", Message)));
            }else{

                List<String> messages = new ArrayList<>();

                if(main.getConfig().contains("User-Notifications." + selectedReport.getReporter()))
                    messages = main.getConfig().getStringList("User-Notifications." + selectedReport.getReporter());
                messages.add(ChatColor.translateAlternateColorCodes('&', main.getUtils().getMessagesConfig().getString("Message-Notification-Format").replace("%sender%", event.getMessage().getAuthor().getDiscriminatedName()).replace("%message%", Message)));
                main.getConfig().set("User-Notifications." + selectedReport.getReporter(), messages);
                main.saveConfig();
            }
        }
    }

  }
