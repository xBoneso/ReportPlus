package me.xbones.reportplus.spigot.MySQL;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import me.xbones.reportplus.spigot.ReportPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class MySQL
{
    private static Connection con;
    private static String host, database, user, password;
    private static String port;

    public MySQL(ReportPlus main) {
        host = main.getConfig().getString("Enabled-Modules.MySQL.Host");
        port = String.valueOf(main.getConfig().getInt("Enabled-Modules.MySQL.Port"));
        database = main.getConfig().getString("Enabled-Modules.MySQL.Database");
        user = main.getConfig().getString("Enabled-Modules.MySQL.Username");
        password = main.getConfig().getString("Enabled-Modules.MySQL.Password");
    }

    public static Connection getConnection()
    {
        return con;
    }

    public static void setConnection(String host, String user, String password, String database, String port) {
        if ((host == null) || (user == null) || (password == null) || (database == null)) {
            return;
        }
        disconnect(false);
        try {
            con = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "MySQL connected.");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MySQL Connect Error: " + e.getMessage());
        }
    }

    public static void connect() {
        connect(true);
    }

    private static void connect(boolean message) {

        if (isConnected()) {
            if (message) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MySQL Connect Error: Already connected");
            }
        } else if (host.equalsIgnoreCase("")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Config Error: Host is blank");
        } else if (user.equalsIgnoreCase("")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Config Error: User is blank");
        } else if (password.equalsIgnoreCase("")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Config Error: Password is blank");
        } else if (database.equalsIgnoreCase("")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Config Error: Database is blank");
        } else if (port.equalsIgnoreCase("")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Config Error: Port is blank");
        } else {
            setConnection(host, user, password, database, port);
        }
    }

    public static void disconnect() {
        disconnect(true);
    }

    private static void disconnect(boolean message) {
        try {
            if (isConnected()) {
                con.close();
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "MySQL disconnected.");
            } else if (message) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MySQL Disconnect Error: No existing connection");
            }
        } catch (Exception e) {
            if (message) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MySQL Disconnect Error: " + e.getMessage());
            }
        }
        con = null;
    }

    public static void reconnect() {
        disconnect();
        connect();
    }

    public static boolean isConnected() {
        return getConnection() != null;
    }

    public static void update(String command) {
        if (command == null) {
            return;
        }
        connect(false);
        try {
            Statement st = getConnection().createStatement();
            st.executeUpdate(command);
            st.close();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MySQL Update:");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Command: " + command);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: " + e.getMessage());
        }
    }

    public static java.sql.ResultSet query(String command) {
        if (command == null) {
            return null;
        }
        connect(false);
        java.sql.ResultSet rs = null;
        try {
            Statement st = getConnection().createStatement();
            rs = st.executeQuery(command);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MySQL Query:");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Command: " + command);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: " + e.getMessage());
        }
        return rs;
    }
    public static boolean tableExists(String table) {
        if (table == null) {
            return false;
        }
        try {
            java.sql.Connection connection = MySQL.getConnection();
            if (connection == null) {
                return false;
            }
            java.sql.DatabaseMetaData metadata = connection.getMetaData();
            if (metadata == null) {
                return false;
            }
            ResultSet rs = metadata.getTables(null, null, table, null);
            if (rs.next()) {
                return true;
            }
        }
        catch (Exception localException) {}
        return false;
    }

    public static void insertData(String columns, String values, String table) {
        MySQL.update("INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")");
    }

    public static void deleteData(String column, String logic_gate, String data, String table) {
        if (data != null) {
            data = "'" + data + "'";
        }
        MySQL.update("DELETE FROM " + table + " WHERE " + column + logic_gate + data + ";");
    }

    public static boolean exists(String column, String data, String table) {
        if (data != null) {
            data = "'" + data + "'";
        }
        try {
            ResultSet rs = MySQL.query("SELECT * FROM " + table + " WHERE " + column + "=" + data);
            while (rs.next()) {
                if (rs.getString(column) != null) {
                    return true;
                }
            }
        }
        catch (Exception localException) {}

        return false;
    }

    public static void deleteTable(String table) {
        MySQL.update("DROP TABLE " + table + ";");
    }

    public static void truncateTable(String table) {
        MySQL.update("TRUNCATE TABLE " + table + ";");
    }

    public static void createTable(String table, String columns) {
        if (!tableExists(table)) {
            MySQL.update("CREATE TABLE " + table + " (" + columns + ")");
        }
    }

    public static void set(String selected, Object object, String column, String logic_gate, String data, String table) {
        if (object != null) {
            object = "'" + object + "'";
        }
        if (data != null) {
            data = "'" + data + "'";
        }
        MySQL.update("UPDATE " + table + " SET " + selected + "=" + object + " WHERE " + column + logic_gate + data + ";");
    }

    public static Object get(String selected, String column, String logic_gate, String data, String table) {
        if (data != null) {
            data = "'" + data + "'";
        }
        try {
            ResultSet rs = MySQL.query("SELECT * FROM " + table + " WHERE " + column + logic_gate + data);
            if (rs.next()) {
                return rs.getObject(selected);
            }
        }
        catch (Exception localException) {}

        return null;
    }

    public int countRows(String table) {
        int i = 0;
        if (table == null) {
            return i;
        }
        ResultSet rs = MySQL.query("SELECT * FROM " + table);
        try {
            while (rs.next()) {
                i++;
            }
        }
        catch (Exception localException) {}

        return i;
    }
}

