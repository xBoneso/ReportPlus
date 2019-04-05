package me.xbones.reportplus.spigot.api;

import me.xbones.reportplus.spigot.Report;
import me.xbones.reportplus.spigot.ReportPlus;
import me.xbones.reportplus.spigot.exception.ExceptionType;
import me.xbones.reportplus.spigot.exception.ReportPlusException;
import me.xbones.reportplus.spigot.punishments.Punishment;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class    ReportPlusAPI {

    private ReportPlus main;


    public ReportPlusAPI(ReportPlus plugin){
        this.main = plugin;
    }

    public  void reportToDiscord(Player reporter, String reported, String content){
        main.reportToDiscord(reporter,reported,content);
    }

    public  void reportToMinecraft(Player reporter, String reported, String content){
        main.reportToStaff(reporter,reported,content);



    }

    public List<Report> getReports(){
        return main.getReportsList();
    }

    /*
    public void setReportClickInventory(Inventory customInv){
        main.getInventoryManager().setCustomCloseReportInventory(customInv);
    }
*/

    public  boolean sendPunishment(Punishment punishment){
        if(main.getConfig().getBoolean("Enabled-Modules.Punishment-Broadcast.Enabled")) {
            if(main.getConfig().getString("Enabled-Modules.Punishment-Broadcast.Channel-ID").equalsIgnoreCase("[INSERT ID HERE]")){
                throw new ReportPlusException(
                        "Punishment broadcasting is not configured.", ExceptionType.NOT_CONFIGURED);

            }
            sendMessageToChannel(main.getConfig().getString("Enabled-Modules.Punishment-Broadcast.Channel-ID"), new EmbedBuilder()
                    .setTitle("New Punishment")
                    .setDescription("You have received a new punishment!")
                    .setColor(Color.RED)
                    .addField("Punisher", punishment.getPunisher(), false)
                    .addField("Punished", punishment.getPunished(), false)
                    .addField("Type", punishment.getType().toString(), false));
            return true;
        }else{
            throw new ReportPlusException(
                    "Punishment broadcasting is disabled.", ExceptionType.DISABLED);
        }
    }

    public  void sendMessageToChannel(String channelID, String message){
        main.getBot().getTextChannelById(channelID).get().sendMessage(message);
    }

    public void sendMessageToChannel(String channelID, EmbedBuilder embed){
        try {

            main.getBot().getTextChannelById(channelID).get().sendMessage(embed);
        } catch(Exception ex){

            throw new ReportPlusException(
                    "Channel not found.", ExceptionType.CHANNEL_DOES_NOT_EXIST);
        }
    }
}
