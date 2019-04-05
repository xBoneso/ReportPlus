package me.xbones.reportplus.bungee.punishments;

import net.md_5.bungee.api.config.ServerInfo;

public class Punishment {

    private String punisher;
    private String punished;
    private String reason;
    private PunishmentType type;
    private ServerInfo server;

    public Punishment(String punisher, String punished, String reason, PunishmentType type, ServerInfo server){
        this.punisher=punisher;
        this.punished=punished;
        this.reason=reason;
        this.type=type;
        this.server=server;
    }

    public  void setServer(ServerInfo server){this.server=server;}
    public ServerInfo getServer() {
        return server;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getPunished() {
        return punished;
    }

    public String getPunisher() {
        return punisher;
    }

    public String getReason() {
        return reason;
    }

    public void setPunished(String punished) {
        this.punished = punished;
    }

    public void setPunisher(String punisher) {
        this.punisher = punisher;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setType(PunishmentType type) {
        this.type = type;
    }
}
