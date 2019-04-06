package me.xbones.reportplus.core.configuration;

import org.bukkit.configuration.file.FileConfiguration;

public class SpigotConfig  {

    private FileConfiguration config;

    public SpigotConfig(Object config) {
        this.config=(FileConfiguration)config;
    }

    public  Object get(String path) {
        return config.get(path);
    }

    public void set(String path, Object value){
        config.set(path,value);
    }
}
