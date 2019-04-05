package me.xbones.reportplus.spigot.punishments;

public class Punishment {

    private String punisher;
    private String punished;
    private String reason;
    private PunishmentType type;

    public Punishment(String punisher, String punished, String reason, PunishmentType type){
        this.punisher=punisher;
        this.punished=punished;
        this.reason=reason;
        this.type=type;
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
