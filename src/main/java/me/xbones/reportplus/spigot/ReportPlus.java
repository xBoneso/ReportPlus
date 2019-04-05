package me.xbones.reportplus.spigot;
import me.xbones.reportplus.DataMessageType;
import me.xbones.reportplus.spigot.Bstats.Metrics;
import me.xbones.reportplus.spigot.MySQL.MySQLManager;
import me.xbones.reportplus.spigot.api.ReportPlusAPI;
import me.xbones.reportplus.spigot.commands.*;
import me.xbones.reportplus.spigot.discord.*;
import me.xbones.reportplus.spigot.events.PlayerReportEvent;
import me.xbones.reportplus.spigot.inventories.InventoryManager;
import me.xbones.reportplus.spigot.listeners.*;
import me.xbones.reportplus.spigot.punishments.Punishment;
import me.xbones.reportplus.spigot.punishments.PunishmentType;
import me.xbones.reportplus.spigot.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.inventivetalent.title.TitleAPI;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.Level;


public class ReportPlus extends JavaPlugin implements PluginMessageListener{

    private Set<String> minecraftChosen = new HashSet<>();
    private Set<String> discordChosen = new HashSet<>();
    private Set<String> bothChosen = new HashSet<>();
    private Set<String> sendingMessage = new HashSet<>();
    private Map<Player, Player> reporting = new HashMap<>();
    private ReportPlus main;
    private DiscordApi bot;
  // private org.javacord.api.DiscordApi reportPlusBot;
    private String ChannelID;
    private String prefix;
    private String CMDChannelID;
    private String MCChannelID;
    private InventoryManager iManager;
    private String lastMessage;
    private String TOKEN;
    private List<String> messagesList = new ArrayList<>();
    private List<Report> reportsList = new ArrayList<>();
    private Chat chat = null;
    private net.milkbowl.vault.permission.Permission permission = null;
    private Utils utils;
    private static ReportPlusAPI api;
    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
    private InventoryClickListener inventoryClickListener;
    private String cmdPrefix;
    private MySQLManager sqlManager;
    private Map<String, Report> selectedReports;

    @Override
    public void onEnable() {
        new Metrics(this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "reportplus:rs");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "reportplus:rs", this);
        this.main = this;

        /*  
        if (!Bukkit.getPluginManager().isPluginEnabled("Discord-ProgramBot-API")) {

            getLogger().severe("*** Discord Bot API is not installed or not enabled. ***");
            getLogger().severe("*** Plugin cannot run. ***");
            Bukkit.getPluginManager().disablePlugin(this);

        } else {re
            getLogger().log(Level.INFO, "*** Discord Bot API found!***");
        }

        */

        if (!Bukkit.getPluginManager().isPluginEnabled("TitleAPI")) {
            getLogger().severe("*** TitleAPI is not installed or not enabled. ***");
            getLogger().severe("*** Titles will not work. ***");
        } else {
            getLogger().log(Level.INFO, "*** TitlesAPI found! Titles working! ***");
        }



        this.getConfig().options().copyDefaults(true);
        saveDefaultConfig();



        initializeVariables();

        if(getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))
        {
           reportsList = sqlManager.getReports();
        }
        else
            utils.createReportsYML();
        utils.createMessagesYML();
        try{
            bot = new DiscordApiBuilder().setToken(TOKEN).login().join();

                if (main.getConfig().getBoolean("Change-Game")) {
                    bot.updateActivity(main.getConfig().getString("Game").replace("%players%",  String.valueOf(Bukkit.getOnlinePlayers().size())));
                    if(main.getConfig().getBoolean("Auto-Refresh-Game")){
                        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                            @Override
                            public void run() {
                                bot.updateActivity(main.getConfig().getString("Game").replace("%players%",  String.valueOf(Bukkit.getOnlinePlayers().size())));
                            }
                        }, 1L, 10 * 20);
                    }
                }
            bot.addMessageCreateListener(new MessageCreateEventListener(main));

            if(this.getConfig().getBoolean("Enabled-Modules.Console")) {

                LogAppender appender = new LogAppender(this);
                logger.addAppender(appender);
            }
        } catch(Exception ex){
            getLogger().severe("The plugin is not configured! Please enter a valid token!");
        }
        if (this.getConfig().getBoolean("Enabled-Modules.Announcements"))
            startAnnouncing();
        initializeCommands();
        initializeEvents();
        setupChat();
        setupPermissions();
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
            public void run(){
                if(main.getConfig().getBoolean("Enabled-Modules.Server-Stop-Start"))
                bot.getTextChannelById(main.getConfig().getString("Server-Stop-Start-Channel")).get().sendMessage(main.getUtils().getMessagesConfig().getString("Server-Start-Message"));

            }
        });

    }

    public void onDisable(){
        if(main.getConfig().getBoolean("Enabled-Modules.Server-Stop-Start"))
            bot.getTextChannelById(this.getConfig().getString("Server-Stop-Start-Channel")).get().sendMessage(main.getUtils().getMessagesConfig().getString("Server-Stop-Message"));
    }
    public void initializeVariables() {
        TOKEN = this.getConfig().getString("Discord-Bot-Token");
        cmdPrefix = this.getConfig().getString("Discord-Bot-Command-Prefix");
        ChannelID = this.getConfig().getString("Discord-Channel-ID");
        prefix = this.getConfig().getString("Prefix");
        CMDChannelID = this.getConfig().getString("Discord-CMD-Channel-ID");
        MCChannelID = this.getConfig().getString("Discord-MC-Channel-ID");
        messagesList = this.getConfig().getStringList("Announcements");
        iManager = new InventoryManager(this);
        iManager.initializeList();
        for(Player p : Bukkit.getOnlinePlayers())
            iManager.initializeReports(p);
        utils = new Utils(this);


        selectedReports = new HashMap<>();
        api = new ReportPlusAPI(this);
        sqlManager = new MySQLManager(this);
    }

    public static ReportPlusAPI getApi() {
        return api;
    }

    //  public DiscordApi getReportPlusBot() {
    //    return reportPlusBot;
    //}


    public Map<String, Report> getSelectedReports() {
        return selectedReports;
    }

    public void initializeCommands() {
        this.getCommand("report").setExecutor(new ReportCommand(this));
        this.getCommand("txtcmd").setExecutor(new TXTCmd(this));
        this.getCommand("cmdcmd").setExecutor(new CmdCMD(this));
        this.getCommand("reports").setExecutor(new ListReportsCMD(this));
        this.getCommand("reportplus").setExecutor(new ReportPlusCommand(this));
        if(this.getConfig().getBoolean("Commands-allowed")) {
            bot.addListener(new me.xbones.reportplus.spigot.discord.ReloadCommand(this));
            bot.addListener(new AddAnnouncementCommand(this));
            bot.addListener(new ListAnnouncementsCommand(this));
            bot.addListener(new HelpCommand(this));
            bot.addListener(new DelAnnouncementCommand(this));
            bot.addListener(new CloseReportWithMessage(this));
        }
        if (this.getConfig().getConfigurationSection("Cmds") != null) {
            Set<String> Cmds;
            Cmds = this.getConfig().getConfigurationSection("Cmds").getKeys(false);
            for (String cmd : Cmds) {
               bot.addListener(new CustomCMD(this, cmd));
            }
        }

        if (this.getConfig().getConfigurationSection("TXTCmds") != null) {
            Set<String> Cmds = new HashSet<>();
            Cmds = this.getConfig().getConfigurationSection("TXTCmds").getKeys(false);
            for (String cmd : Cmds) {
                bot.addListener(new CustomTXTCMD(this, cmd));
            }
        }

    }


    private boolean setupPermissions()
    {
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    public void initializeEvents(){
        PluginManager manager = getServer().getPluginManager();
        inventoryClickListener = new InventoryClickListener(this);
        manager.registerEvents(inventoryClickListener, this);
        manager.registerEvents(new PlayerChatListener(this), this);
        manager.registerEvents(new PlayerCommandListener(this), this);
        manager.registerEvents(new PlayerDeathListener(this), this);
        manager.registerEvents(new PlayerJoinListener(this), this);
        manager.registerEvents(new PlayerLeaveListener(this), this);
        manager.registerEvents(new PlayerReportListener(this),this);
    }


    public InventoryClickListener getInventoryClickListener() {
        return inventoryClickListener;
    }

    public void startAnnouncing() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                Random random = new Random();
                int index = random.nextInt(messagesList.size());
                if (messagesList.get(index).equals(lastMessage))
                    index = random.nextInt(messagesList.size());
                bot.getTextChannelById(main.getConfig().getString("Discord-Announcements-Channel-ID"))
                        .get().sendMessage("```" + messagesList.get(index) + "```");
            }

        },0L, (long)this.getConfig().getInt("Interval") * 20);
    }

    public List<Report> getReportsList() {
        return reportsList;
    }
    public List<String> getMessages(){
        return messagesList;
    }

    public InventoryManager getInventoryManager() {
        return iManager;
    }

    public void sendMessage(List<String> data) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            for(String message : data) {
                out.writeUTF(message);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }

        getServer().sendPluginMessage(this, "reportplus:rs", stream.toByteArray());
    }

    public Set<String> getDiscordChosen() {
        return discordChosen;
    }

    public Map<Player, Player> getReporting() {
        return reporting;
    }

    public ReportPlus getMain() {
        return main;
    }

    public DiscordApi getBot() {
        return bot;
    }

    public Set<String> getMinecraftChosen() {
        return minecraftChosen;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMCChannelID() {
        return MCChannelID;
    }

    public String getChannelID() {
        return ChannelID;
    }

    public String getCMDChannelID() {
        return CMDChannelID;
    }

    public void showGUI(Player p) {
        iManager.initializeReports(p);
        p.openInventory(iManager.getReportInventory());

    }

    public void NoPerm(Player p) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                prefix + " " + main.getUtils().getMessagesConfig().getString("No-Permission")));
    }

    public void sendMessageToChannel(Optional<TextChannel> channel, EmbedBuilder message) {
        channel.get().sendMessage(message).join();
    }

    public void reportToDiscord(Player p,String reported, String Message) {
        PlayerReportEvent event = new PlayerReportEvent(p, reported, Message, ReportType.DISCORD);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        String title = main.getUtils().getMessagesConfig().getString("Discord-Report-Title-Message.Title").replace("%player%", p.getName())
                .replace("%report%", Message);
        String subtitle = main.getUtils().getMessagesConfig().getString("Discord-Report-Title-Message.Subtitle")
                .replace("%player%", p.getName()).replace("%report%", Message);

        String imageLink = "https://cdn.discordapp.com/embed/avatars/0.png";
        Player target = Bukkit.getPlayer(reported);
        if (target != null) {
            imageLink = "https://crafatar.com/avatars/" + target.getUniqueId();
        }
        sendMessageToChannel(bot.getTextChannelById(ChannelID),
                new EmbedBuilder().setDescription(main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Description"))
                        .setColor(new Color(16711682)).setThumbnail(imageLink).setTitle(main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Title"))
                        .addField("Reporter", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Reporter").replace("%reporter%", p.getName()), false)
                        .addField("Reported", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Reported").replace("%reported%", reported), false)
                        .addField("Server", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Server").replace("%server%", Bukkit.getServerName()))
                        .addField("Report ID", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Report-ID").replace("%reportid%", Integer.toString(reportsList.size() + 1)), false)
                        .addField("Report Content", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Report-Content").replace("%reportcontent%", Message), false));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + main.getUtils().getMessagesConfig().getString("Success-Report")));
        Report r = new Report(reportsList.size() + 1, p.getName(),reported, Message, ReportType.DISCORD);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        r.setDate(dtf.format(now));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " &cYour report id is: " + r.getReportId()));
        sqlManager.addReportToDatabase(r);
        reportsList.add(r);
        if(!getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))
            utils.saveReportsToConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("reportplus.receive") || player.isOp()) {
                if (Bukkit.getPluginManager().isPluginEnabled("TitleAPI")) {
                    TitleAPI.sendTitle(player, new TextComponent(ChatColor.translateAlternateColorCodes('&', title)));
                    TitleAPI.sendSubTitle(player, new TextComponent(ChatColor.translateAlternateColorCodes('&', subtitle)));//(player, 10, 10, 10, ChatColor.translateAlternateColorCodes('&', title),
                            //ChatColor.translateAlternateColorCodes('&', subtitle));
                }
            }
        }


    }

    public MySQLManager getSqlManager() {
        return sqlManager;
    }

    public void reportToBoth(Player p, String reported, String Message) {

        PlayerReportEvent event = new PlayerReportEvent(p, reported, Message, ReportType.BOTH);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        List<String> message = new ArrayList<>();
        message.add(DataMessageType.REPORT.toString());
        message.add(p.getName());
        message.add(reported);
        message.add(Message);
        sendMessage(message);
        String title = main.getUtils().getMessagesConfig().getString("Discord-Report-Title-Message.Title").replace("%player%", p.getName())
                .replace("%report%", Message);
        String subtitle = main.getUtils().getMessagesConfig().getString("Discord-Report-Title-Message.Subtitle")
                .replace("%player%", p.getName()).replace("%report%", Message);

        String imageLink = "https://cdn.discordapp.com/embed/avatars/0.png";
        Player target = Bukkit.getPlayer("None");
        if (target != null) {
            imageLink = "https://crafatar.com/avatars/" + target.getUniqueId();
        }
        sendMessageToChannel(bot.getTextChannelById(ChannelID),
                new EmbedBuilder().setDescription(main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Description"))
                        .setColor(new Color(16711682)).setThumbnail(imageLink).setTitle(main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Title"))
                        .addField("Reporter", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Reporter").replace("%reporter%", p.getName()), false)
                        .addField("Reported", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Reported").replace("%reported%", reported), false)
                        .addField("Server", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Server").replace("%server%", Bukkit.getServerName()))
                        .addField("Report ID", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Report-ID").replace("%reportid%", "" + reportsList.size() + 1), false)
                        .addField("Report Content", main.getUtils().getMessagesConfig().getString("Discord-Report-Embed.Fields.Report-Content").replace("%reportcontent%", Message), false));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("reportplus.receive") || player.isOp()) {
                for (String s : main.getUtils().getMessagesConfig().getStringList("Minecraft-Report-Format")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', s.replace("%reporter%", player.getName()).replace("%reported%", reported).replace("%reportcontent%", Message)));

                }
                if (Bukkit.getPluginManager().isPluginEnabled("TitleAPI")) {
                    TitleAPI.sendTitle(player, new TextComponent(ChatColor.translateAlternateColorCodes('&', title)));
                    TitleAPI.sendSubTitle(player, new TextComponent(ChatColor.translateAlternateColorCodes('&', subtitle)));//(player, 10, 10, 10, ChatColor.translateAlternateColorCodes('&', title),
                    //ChatColor.translateAlternateColorCodes('&', subtitle));

                }
            }
        }
        Report r = new Report(reportsList.size() + 1, p.getName(), reported, Message, ReportType.BOTH);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        r.setDate(dtf.format(now));
        sqlManager.addReportToDatabase(r);
        reportsList.add(r);
        if(!getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))
        utils.saveReportsToConfig();
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + main.getUtils().getMessagesConfig().getString("Success-Report")));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " &cYour report id is: " + r.getReportId()));

    }

    public void reportToStaff(Player player, String reported, String Message) {
        PlayerReportEvent event = new PlayerReportEvent(player, reported, Message, ReportType.MINECRAFT);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        List<String> message = new ArrayList<>();
        message.add(DataMessageType.REPORT.toString());
        message.add(player.getName());
        message.add(reported);
        message.add(Message);
        sendMessage(message);
        String title = main.getUtils().getMessagesConfig().getString("Minecraft-Report-Title-Message.Title")
                .replace("%player%", player.getName()).replace("%report%", Message);
        String subtitle = main.getUtils().getMessagesConfig().getString("Minecraft-Report-Title-Message.Subtitle")
                .replace("%player%", player.getName()).replace("%report%", Message);


        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("reportplus.receive") || p.isOp()) {
                for (String s : main.getUtils().getMessagesConfig().getStringList("Minecraft-Report-Format")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', s.replace("%reporter%", player.getName()).replace("%reported%", reported).replace("%reportcontent%", Message)));

                }
                if (Bukkit.getPluginManager().isPluginEnabled("TitleAPI")) {

                    TitleAPI.sendTitle(player, new TextComponent(ChatColor.translateAlternateColorCodes('&', title)));
                    TitleAPI.sendSubTitle(player, new TextComponent(ChatColor.translateAlternateColorCodes('&', subtitle)));//(player, 10, 10, 10, ChatColor.translateAlternateColorCodes('&', title),
                    //ChatColor.translateAlternateColorCodes('&', subtitle));

                }
            }
        }

        Report r = new Report(reportsList.size() + 1, player.getName(), reported, Message, ReportType.MINECRAFT);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        r.setDate(dtf.format(now));
        sqlManager.addReportToDatabase(r);
        reportsList.add(r);
        if(!getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))
        utils.saveReportsToConfig();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + main.getUtils().getMessagesConfig().getString("Success-Report")));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " &cYour report id is: " + r.getReportId()));
    }



    public void AddTextCMD(CommandSender p, String cmd, String text) {
        main.getConfig().set("TXTCmds." + cmd + ".text", text);
        main.getConfig().set("TXTCmds." + cmd + ".description", "My cmd! (Configure this in the config.yml)");
        main.saveConfig();
        main.reloadConfig();

        bot.addListener(new CustomTXTCMD(this, cmd));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getPrefix() + " &aSuccess!"));
    }

    public void AddCMDCMD(CommandSender p, String cmd, String cmdtobeexecuted) {
        main.getConfig().set("Cmds." + cmd + ".targetcmd", cmdtobeexecuted);
        main.getConfig().set("Cmds." + cmd + ".description", "My cmd! (Configure this in the config.yml)");
        main.getConfig().set("Cmds." + cmd + ".say", "My cmd executed! (Configure this in the config.yml)");
        main.saveConfig();
        main.reloadConfig();

        bot.addListener(new CustomCMD(this, cmd));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getPrefix() + " &aSuccess!"));
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("reportplus:rs")) {
            return;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(stream);
        try {
            System.out.println("Received message.");
            System.out.println(in.readUTF());
            if(DataMessageType.valueOf(in.readUTF()).equals(DataMessageType.PUNISHMENT)){
                System.out.println("Received punishement.");
                String serverName = in.readUTF();
                    String punished = in.readUTF();
                    String punisher = in.readUTF();
                    String reason = in.readUTF();
                    PunishmentType type = PunishmentType.valueOf(in.readUTF());
                    Punishment punishment = new Punishment(punisher,punished,reason,type);

                    getApi().sendPunishment(punishment);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Utils getUtils() {
        return utils;
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    public void setReportsList(List<Report> reportsList) {
        this.reportsList = reportsList;
    }

    public Chat getChat() {
        return chat;
    }

    public net.milkbowl.vault.permission.Permission getPermission() {
        return permission;
    }

    public Set<String> getBothChosen() {
        return bothChosen;
    }

    public  String getCmdPrefix(){
        return cmdPrefix;
    }

    public Set<String> getSendingMessage() {
        return sendingMessage;
    }
}
