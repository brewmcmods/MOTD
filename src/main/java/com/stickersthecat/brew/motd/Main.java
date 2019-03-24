package com.stickersthecat.brew.motd;

import java.time.ZonedDateTime;
import java.util.Calendar;

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

    public String StringFilter(String Old) {

        String New = Old;

        // do time
        int Hour = ZonedDateTime.now().getHour();
        String HourS = String.format("%1$" + 2 + "s", Hour).replace(' ', '0');
        int Minute = ZonedDateTime.now().getMinute();
        String MinuteS = String.format("%1$" + 2 + "s", Minute).replace(' ', '0');
        New = New.replace("{EARTH_TIME}", HourS + ":" + MinuteS);

        return New;
    }
}
