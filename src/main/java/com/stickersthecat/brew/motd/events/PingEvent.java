package com.stickersthecat.brew.motd.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.stickersthecat.brew.motd.Main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class PingEvent implements Listener {

    private Main Plugin;

    public PingEvent(Main Plugin) {

        this.Plugin = Plugin;
    }

    @EventHandler
    public void OnPing(ServerListPingEvent Event) {

        // make sure MOTD exists and its enabled (using default of true if enabled
        // doesnt exist)
        if (this.Plugin.Config.contains("motd", true) && this.Plugin.Config.getBoolean("motd.enabled", true)) this.MOTD(Event);
    }

    public void MOTD(ServerListPingEvent Event) {

        String MOTD = "";

        // if this doesnt exist; exit
        if (!this.Plugin.Config.contains("motd.mode")) return;

        switch (this.Plugin.Config.getString("motd.mode").toLowerCase()) {

        case "static":
            MOTD = this.StaticMOTD();
            break;
        case "random":
            MOTD = this.RandomMOTD();
            break;
        case "earthtime":
            MOTD = this.EarthTimeMOTD();
            break;
        default:
            break;
        }

        // if fail use static
        if (MOTD == "" && this.Plugin.Config.contains("motd.static", true)) {

            this.Plugin.getLogger().info("Something want wrong selecting a MOTD, attempting to fall back to the static MOTD"); // error: major failure
            MOTD = this.StaticMOTD();
        }

        // check it again and if this fails allow it to default to the server prop one
        if (MOTD != "") {

            Event.setMotd(MOTD);
        } else {

            this.Plugin.getLogger().info("Something want really wrong, using MOTD defined by server.properties"); // error: really major failure
        }
    }

    /**
     * Root Level Static MOTD
     */
    private String StaticMOTD() {

        return this.StaticKey("motd.static");
    }

    /**
     * so simple it probally doesnt even need its own func... just like 2 of the
     * MOTD funcs! but it does! so deal with it
     * 
     * ... never code while bored kids
     */
    private String StaticKey(String Key) {

        String MOTD = "";

        // 1st make sure the key is set
        if (!this.Plugin.Config.contains(Key, true)) {

            this.Plugin.getLogger().info(Key + ": is not defined but was selected for use as the MOTD."); // error: static key not defined
            return MOTD;
        }
        // now build it
        return this.BuildMOTD(this.Plugin.Config.getStringList(Key));
    }

    /**
     * Root Level random MOTD
     */
    private String RandomMOTD() {

        return this.RandomKey("motd.random");
    }

    /**
     * Use to select random MOTD
     */
    private String RandomKey(String Key) {

        String MOTD = "";

        // 1st make sure the key is set
        if (!this.Plugin.Config.contains(Key)) {

            this.Plugin.getLogger().info(Key + ": is not defined but is the selected key for the MOTD.");
            return MOTD;
        }

        // next get all the results and assign to array
        Set<String> Set = this.Plugin.Config.getConfigurationSection(Key).getKeys(false);
        String[] Array = Set.toArray(new String[Set.size()]);

        // make sure array is filled
        if (Array.length <= 0) {

            this.Plugin.getLogger().info(Key + ": Has no random MOTD's defined.");
            return MOTD;
        }

        // select a key
        int Random = new Random().nextInt(Array.length);
        String Selected = Key + "." + Array[Random];

        // double check to be safe
        if (this.Plugin.Config.contains(Selected)) MOTD = this.BuildMOTD(this.Plugin.Config.getStringList(Selected));

        return MOTD;
    }

    private String EarthTimeMOTD() {

        String MOTD = "";

        // start this one off by making sure the time key exists
        if (!this.Plugin.Config.contains("motd.earthtime")) {

            this.Plugin.getLogger().info("The Key motd.earthtime isn't defined, but you are trying to use it.");
            return MOTD;
        }

        // now get current time frame
        String Frame = this.EarthTimeSelect();

        // if empty fail
        if (Frame == "") return this.EarthTimeDefault();

        // if no mode is set fail
        if (!this.Plugin.Config.contains(Frame + ".mode")) {

            this.Plugin.getLogger().info("The Frame @ " + Frame + " Has no defined mode, using earthtime's default.");
            return this.EarthTimeDefault();
        }

        switch (this.Plugin.Config.getString(Frame + ".mode").toLowerCase()) {

        case "random":
            MOTD = this.RandomKey(Frame + ".random");
            break;
        case "static":
            MOTD = this.StaticKey(Frame + ".static");
            break;
        default:
            this.Plugin.getLogger().info("Error Selecting mode for " + Frame + " Selected " + this.Plugin.Config.getString(Frame + ".mode"));
            break;
        }

        if (MOTD == "") {

            this.Plugin.getLogger().info("No MOTD was able to be used at frame " + Frame + " Please check your config");
            MOTD = this.EarthTimeDefault();
        }

        return MOTD;
    }

    /**
     * Loop throw the time until we find one that matches then return full key
     */
    private String EarthTimeSelect() {

        String Key = "";
        Set<String> Frames = this.Plugin.Config.getConfigurationSection("motd.earthtime.frames").getKeys(false);

        // make sure list isnt empty
        if (Frames.isEmpty()) {

            this.Plugin.getLogger().info("You Selected EarthTime MOTD but did not include any frames.");
            return Key;
        }

        for (String Value : Frames) {

            // make sure both time values are set
            if (!this.Plugin.Config.contains("motd.earthtime.frames." + Value + ".from")
                    && !this.Plugin.Config.contains("motd.earthtime.frames." + Value + ".till")) {

                this.Plugin.getLogger().info("motd.earthtime.frames." + Value + " is not setup correctly, skipping it.");
                return Key;
            }

            // next do the time compares
            String TimeFrom = this.Plugin.Config.getString("motd.earthtime.frames." + Value + ".from");
            String TimeTo = this.Plugin.Config.getString("motd.earthtime.frames." + Value + ".till");

            if (this.timeCompare(TimeFrom, true) && this.timeCompare(TimeTo, false)) {

                Key = "motd.earthtime.frames." + Value;
                break;
            }
        }

        return Key;
    }

    private String EarthTimeDefault() {

        String MOTD = "";

        // make sure exists
        if (this.Plugin.Config.contains("motd.earthtime.default")) return MOTD;

        switch (this.Plugin.Config.getString("motd.earthtime.default").toLowerCase()) {

        case "random":
            MOTD = this.RandomMOTD();
            break;
        case "static":
            MOTD = this.StaticMOTD();
            break;
        }

        return MOTD;
    }

    private boolean timeCompare(String Time, boolean Greater) {

        try {

            SimpleDateFormat Format = new SimpleDateFormat("HH:mm");
            Date FrameTime = Format.parse(Time);
            int Hour = ZonedDateTime.now().getHour();
            int Minute = ZonedDateTime.now().getMinute();
            Date TimeNow = Format.parse(Hour + ":" + Minute);

            // if greater, see if time is after
            if (Greater) {

                if (TimeNow.after(FrameTime)) {

                    return true;
                } else if (TimeNow.equals(FrameTime)) {

                    return true;
                }
            } else { // assume greater is false, run before

                if (TimeNow.before(FrameTime)) {

                    return true;
                } else if (TimeNow.equals(FrameTime)) {

                    return true;
                }
            }
        } catch (ParseException e) {

            this.Plugin.getLogger().info(e.getMessage());
        }

        return false;
    }

    public String BuildMOTD(List<String> Values) {

        StringBuilder MOTD = new StringBuilder();
        int I = 0;

        for (String Value : Values) {

            MOTD.append(this.Plugin.StringFilter(Value) + (I == 0 ? "\n" : ""));
            I++;

            // dont allow more then 2 lines cause... well ya
            if (I > 1) break;
        }

        return MOTD.toString();
    }
}