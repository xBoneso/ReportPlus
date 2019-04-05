package me.xbones.reportplus.bungee.api;

import me.xbones.reportplus.DataMessageType;
import me.xbones.reportplus.bungee.ReportPlus;
import me.xbones.reportplus.bungee.punishments.Punishment;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.Map;

public class ReportPlusBungeeAPI {

    private ReportPlus main;

    public ReportPlusBungeeAPI(ReportPlus main){
        this.main=main;
    }

    public void sendMessageToAllServers(TextComponent message){
        for(Map.Entry<String, ServerInfo> server : main.getProxy().getServers().entrySet()) {
            ServerInfo serverInfo = server.getValue();
            for (ProxiedPlayer p : serverInfo.getPlayers()) {
                p.sendMessage(message);
            }
        }
    }


    public void sendMessageToServer(ServerInfo server, TextComponent message){
        for(ProxiedPlayer p : server.getPlayers()){
            p.sendMessage(message);
        }
    }
    private TextComponent getTextComponent(String msg, ServerInfo server) {
        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', msg));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to go there!").create() ));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server.getName()));
        return message;
    }

    public  void sendMessageToAllServers(Punishment punishment){
        ServerInfo server;
        if(punishment.getServer() == null)
        {
            server =  ProxyServer.getInstance().constructServerInfo("Global", InetSocketAddress.createUnresolved("127.0.0.1", 25565),"Global", false);

        }
        else
             server = punishment.getServer();
        punishment.setServer(server);
        main.sendMessage(punishment, server, DataMessageType.PUNISHMENT);
        /*
        for(Map.Entry<String, ServerInfo> server : main.getProxy().getServers().entrySet()) {
            ServerInfo serverInfo = server.getValue();
            for (ProxiedPlayer p : serverInfo.getPlayers()) {
                if (p.hasPermission("reportplus.receive")) {
                    p.sendMessage(getTextComponent("&8&m          I         ", punishment.getServer()));
                    p.sendMessage(getTextComponent("", punishment.getServer()));
                    p.sendMessage(getTextComponent("          &6&lNew &c&lPunishment!         ", punishment.getServer()));
                    p.sendMessage(getTextComponent("", punishment.getServer()));
                    p.sendMessage(getTextComponent("          &6Punisher: &c" + punishment.getPunisher() + "          ", punishment.getServer()));
                    p.sendMessage(getTextComponent("", punishment.getServer()));
                    p.sendMessage(getTextComponent("          &6Punished: &c" + punishment.getPunished() + "          ", punishment.getServer()));
                    p.sendMessage(getTextComponent("", punishment.getServer()));
                    p.sendMessage(getTextComponent("          &6Punishment Reason: &c" + punishment.getReason() + "          ", punishment.getServer()));
                    p.sendMessage(getTextComponent("", punishment.getServer()));
                    p.sendMessage(getTextComponent("          &6Server: &c" + punishment.getServer().getName() + "          ", punishment.getServer()));
                    p.sendMessage(getTextComponent("", punishment.getServer()));
                    p.sendMessage(getTextComponent("&8&m          I         ", punishment.getServer()));

                }

            }
        }

        */
    }
}
