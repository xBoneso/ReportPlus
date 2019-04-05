package me.xbones.reportplus.bungee;

import me.xbones.reportplus.DataMessageType;
import me.xbones.reportplus.bungee.api.ReportPlusBungeeAPI;
import me.xbones.reportplus.bungee.punishments.Punishment;
import me.xbones.reportplus.spigot.Report;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.apache.logging.log4j.Level;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class ReportPlus extends Plugin implements Listener {

    private static ReportPlusBungeeAPI api;
    private DiscordApi bot;
	private static final java.util.logging.Logger logger = ProxyServer.getInstance().getLogger();
    private Configuration configuration;
    private List<Report> listOfreports;
    public void onEnable() {
		this.getProxy().registerChannel("reportplus:rs");
        this.getProxy().getPluginManager().registerListener(this, this);
        this.getProxy().getPluginManager().registerListener(this, new PlayerMessageListener(this));
        api= new ReportPlusBungeeAPI(this);
        listOfreports = new ArrayList<>();


        try{
            initConfig();
            bot = new DiscordApiBuilder().setToken((String)getValue("Discord-Bot-Token", "[INSERT TOKEN HERE]")).login().join();
            if ((boolean)getValue("Change-Game", true)) {
                bot.updateActivity(((String)getValue("Game", "%players% are currently online")).replace("%players%",  String.valueOf(getProxy().getOnlineCount())));
                if ((boolean)getValue("Auto-Refresh-Game", true)) {
                    getProxy().getScheduler().schedule(this, new Runnable() {
                        @Override
                        public void run() {
                            bot.updateActivity(((String)getValue("Game", "%players% are currently online")).replace("%players%",  String.valueOf(getProxy().getOnlineCount())));
                        }
                    }, 1L, Long.valueOf((int)getValue("Refresh-Interval", 20)), TimeUnit.SECONDS);
                }
            }
            bot.addMessageCreateListener(new MessageCreateEventListener(this));
            if((boolean)getValue("Enabled-Modules.Console", true))
            logger.addHandler(new LogAppender(this));

        } catch(Exception ex){
            getLogger().severe("The plugin is not configured! Please enter a valid token!");
        }
	}

    public DiscordApi getBot() {
        return bot;
    }

    public Configuration getConfig() {
        return configuration;
    }
    public Object getValue(String path,Object defval) {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File file = new File(getDataFolder(), "config.yml");

            if (!file.exists()) {
                file.createNewFile();

            }
                configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(path != null && defval != null) {
            if(configuration.get(path) == null) {
                configuration.set(path,defval);
                //saveFiles(config,configfile);
                try {
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            return configuration.get(path);
        }
        return null;
    }

    public void initConfig(){
        getValue("Enabled-Modules.Console", true);
        getValue("Enabled-Modules.Chat-Sync", true);

        getValue("Discord-Bot-Token", "[INSERT TOKEN HERE]");
        getValue("Change-Game", true);
        getValue("Auto-Refresh-Game", true);
        getValue("Refresh-Interval", 10);
        getValue("Game", "%players% are currently online");
        getValue("Console-Channel-ID", "[INSERT ID HERE]");
        getValue("Chat-Sync-Channel-ID", "[INSERT ID HERE]");
        getValue("Chat-Sync-Format", "%player% -> %message%");
        getValue("Discord-Chat-Sync-Format", "&b[Discord] &c%user% -> %message%");
    }

	public static ReportPlusBungeeAPI getAPI(){
	    return api;
    }
	public void sendMessage(String message, ServerInfo server, DataMessageType type) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			out.writeUTF(type.toString());
			out.writeUTF(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        server.sendData("reportplus:rs", stream.toByteArray());
    }

    public void sendMessage(Punishment message, ServerInfo server, DataMessageType type) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(type.toString());
            out.writeUTF(server.getName());
            out.writeUTF(message.getPunished());
            out.writeUTF(message.getPunisher());
            out.writeUTF(message.getReason());
            out.writeUTF(message.getType().toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Sent punishment. ");
        this.getProxy().getPlayer(message.getPunisher()).getServer().sendData("reportplus:rs", stream.toByteArray());
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
        if (!ev.getTag().equals("reportplus:rs")) {
            return;
        }

        if (!(ev.getSender() instanceof Server)) {
            return;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
			DataMessageType type = DataMessageType.valueOf(in.readUTF());
			if(type == DataMessageType.REPORT) {
				String reporter = in.readUTF();
				String reported = in.readUTF();
				String report = in.readUTF();
				ProxiedPlayer p = getProxy().getPlayer(reporter);
				ServerInfo serverName = p.getServer().getInfo();
				sendReport(reporter, reported, report, serverName);
				System.out.println("Received a report!");
			} else if(type == DataMessageType.LOG){
				String closer = in.readUTF();
				boolean discord = Boolean.valueOf(in.readUTF());
				String id = in.readUTF();
				ServerInfo server;
				if(!discord)
				    server = getProxy().getPlayer(closer).getServer().getInfo();
				else
                    server =  ProxyServer.getInstance().constructServerInfo("Discord", InetSocketAddress.createUnresolved("127.0.0.1", 25565), "Discord", false);

                String content = in.readUTF();
				sendLog(closer,id,content,server);
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public TextComponent getTextComponent(String msg, ServerInfo server) {
		TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', msg));
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to go there!").create() ));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server.getName()));
		return message;
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
          if (hm.get(o).equals(value)) {
            return o;
          }
        }
        return null;
      }

      public void sendLog(String closer, String id, String content, ServerInfo serverName){
		  Map<String, ServerInfo> servers = getProxy().getServers();
		  servers.remove(getKeyFromValue(servers, serverName));
		  for(Entry<String, ServerInfo> server : servers.entrySet()) {
			  ServerInfo serverInfo = server.getValue();
			  for(ProxiedPlayer p : serverInfo.getPlayers()) {
				  if (p.hasPermission("reportplus.receive") ) {
					  p.sendMessage(getTextComponent("&8&m          I         ", serverName));
					  p.sendMessage(getTextComponent("", serverName));
					  p.sendMessage(getTextComponent("          &6&lReport &c&lClosed!         ", serverName));
					  p.sendMessage(getTextComponent("", serverName));
					  p.sendMessage(getTextComponent("          &6Closer: &c" + closer + "          ", serverName));
					  p.sendMessage(getTextComponent("", serverName));
					  p.sendMessage(getTextComponent("          &6Report ID: &c" + id + "          ", serverName));
					  p.sendMessage(getTextComponent("", serverName));
					  p.sendMessage(getTextComponent("          &6Report Reason: &c" + content + "          ", serverName));
					  p.sendMessage(getTextComponent("", serverName));
					  p.sendMessage(getTextComponent("          &6Server: &c" + serverName.getName() + "          ", serverName));
					  p.sendMessage(getTextComponent("", serverName));
					  p.sendMessage(getTextComponent("&8&m          I         ", serverName));
				  }
			  }
		  }
	  }

    public void sendReport(String reporter, String reported, String report, ServerInfo serverName) {
    	Map<String, ServerInfo> servers = getProxy().getServers();
    	servers.remove(getKeyFromValue(servers, serverName));
    	for(Entry<String, ServerInfo> server : servers.entrySet()) {
    		ServerInfo serverInfo = server.getValue();
    		for(ProxiedPlayer p : serverInfo.getPlayers()) {
    			if (p.hasPermission("reportplus.receive")) {
    				p.sendMessage(getTextComponent("&8&m          I         ", serverName));
    				p.sendMessage(getTextComponent("", serverName));
    				p.sendMessage(getTextComponent("          &6&lNew &c&lReport!         ", serverName));
    				p.sendMessage(getTextComponent("", serverName));
    				p.sendMessage(getTextComponent("          &6Reporter: &c" + reporter + "          ", serverName));
    				p.sendMessage(getTextComponent("", serverName));
    				p.sendMessage(getTextComponent("          &6Reported: &c" + reported + "          ", serverName));
    				p.sendMessage(getTextComponent("", serverName));
    				p.sendMessage(getTextComponent("          &6Reason: &c" + report + "          ", serverName));
    				p.sendMessage(getTextComponent("", serverName));
    				p.sendMessage(getTextComponent("          &6Server: &c" + serverName.getName() + "          ", serverName));
    				p.sendMessage(getTextComponent("", serverName));
    				p.sendMessage(getTextComponent("&8&m          I         ", serverName));

    			}
    		}
    	}
    }
}
