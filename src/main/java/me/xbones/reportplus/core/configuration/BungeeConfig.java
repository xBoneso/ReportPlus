package me.xbones.reportplus.core.configuration;

import net.md_5.bungee.config.Configuration;

public class BungeeConfig {

    private Configuration config;

    public BungeeConfig(Object config){
        this.config=(Configuration)config;
    }

    public  Object get(String path) {
        return config.get(path);
    }

    public void set(String path, Object value){
        config.set(path,value);
    }
}
