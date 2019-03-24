package com.stickersthecat.brew.motd;

import com.stickersthecat.brew.motd.events.PingEvent;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public FileConfiguration Config;
    
    @Override
    public void onEnable() {

        // do config
        this.saveDefaultConfig();
        this.Config = this.getConfig();

        this.getServer().getPluginManager().registerEvents(new PingEvent(this), this);
    }
}
