package me.xbones.reportplus.spigot.MySQL;

import me.xbones.reportplus.spigot.Report;
import me.xbones.reportplus.spigot.ReportPlus;
import me.xbones.reportplus.spigot.ReportType;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLManager {

    private ReportPlus main;

    private String host, database, username, password;
    private int port;

    public MySQLManager(ReportPlus main) {
        this.main = main;

        if (main.getConfig().getBoolean("Enabled-Modules.MySQL.Enabled")) {
            try {
                new MySQL(main);
                MySQL.connect();

                createTable();

                //   StartRepeatingTimer(main.getConfig().getInt("Enabled-Modules.MySQL.Refresh-Interval"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void StartRepeatingTimer(int seconds) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> refresh(), 0L, seconds * 20);
    }

    public void refresh(){
        if(main.getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))
            main.setReportsList(getReports());
    }

    public void createTable(){
        try {
            MySQL.createTable("reports"," id INT PRIMARY KEY," +
                    " content LONGTEXT," +
                    " reporter TEXT," +
                    " reported TEXT," +
                    " type TEXT, " +
                    " date TEXT ");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addReportToDatabase(Report report){
        try {
            if(!main.getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))return;

            MySQL.insertData("id, content, reporter, reported, type, date",
                    "'" + report.getReportId() + "'" +
                            " , '" + report.getReportContent() + "'" +
                            " , '" + report.getReporter() + "'" +
                            " , '" + report.getReported() + "'" +
                            " , '" + report.getType().toString() + "'" +
                            " , '" + report.getDate() + "'",

                    "reports");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removeReportFromDatabase(Report report){
        try{
            if(!main.getConfig().getBoolean("Enabled-Modules.MySQL.Enabled"))return;

            MySQL.deleteData("id", "=", String.valueOf(report.getReportId()), "reports");
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public List<Report> getReports(){
        List<Report> reports = new ArrayList<>();

        try {
            ResultSet rs = MySQL.query("SELECT * from reports");
            while(rs.next())
            {
                Report report = new Report(
                        rs.getInt("id"),
                        rs.getString("reporter"),
                        rs.getString("reported"),
                        rs.getString("content"),
                        ReportType.valueOf(rs.getString("type")));
                report.setDate(rs.getString("date"));
                reports.add(report);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return reports;
    }

}
