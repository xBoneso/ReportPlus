package me.xbones.reportplus.core.configuration;


public class ConfigurationManager {

    private static boolean Bungeecord;
    private static SpigotConfig spigotConfig;
    private static BungeeConfig bungeeConfig;

    public ConfigurationManager(boolean Bungeecord){

        this.Bungeecord=Bungeecord;
    }

    public static void SetConfig(Object object){
        if(Bungeecord){
            bungeeConfig = new BungeeConfig(object);
        }else{
            spigotConfig = new SpigotConfig(object);
        }
    }

    public static Object get(String path){
        if(Bungeecord){
           return bungeeConfig.get(path);
        }else{
            return spigotConfig.get(path);
        }
    }

    public static void set(String path, Object value){
        if(Bungeecord){
            bungeeConfig.set(path,value);
        }else{
            spigotConfig.set(path,value);
        }
    }
}
