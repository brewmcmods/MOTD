package com.stickersthecat.brew.motd.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        // make sure MOTD exists
        if (this.Plugin.Config.contains("motd"))
            this.MOTD(Event);
    }

    public void MOTD(ServerListPingEvent Event) {

        String MOTD = "";

        // if this doesnt exist; exit
        if (!this.Plugin.Config.contains("motd.mode"))
            return;

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
        if (MOTD == "" && this.Plugin.Config.contains("motd.static"))
            MOTD = this.BuildMOTD(this.Plugin.Config.getStringList("motd.static"));

        // check it again and if this fails allow it to default to the server prop one
        if (MOTD != "")
            Event.setMotd(MOTD);
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
        if (!this.Plugin.Config.contains(Key))
            return MOTD;

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
        if (!this.Plugin.Config.contains(Key))
            return MOTD;

        // next get all the results and assign to array
        Set<String> Set = this.Plugin.Config.getConfigurationSection(Key).getKeys(false);
        String[] Array = Set.toArray(new String[Set.size()]);

        // make sure array is filled
        if (Array.length <= 0)
            return MOTD;

        // select a key
        int Random = new Random().nextInt(Array.length);
        String Selected = Key + "." + Array[Random];

        // double check to be safe
        if (this.Plugin.Config.contains(Selected))
            MOTD = this.BuildMOTD(this.Plugin.Config.getStringList(Selected));

        return MOTD;
    }

    private String EarthTimeMOTD() {

        String MOTD = "";

        // start this one off by making sure the time key exists
        if (!this.Plugin.Config.contains("motd.earthtime"))
            return MOTD;

        // now get current time frame
        String Frame = this.EarthTimeSelect();

        // if empty fail
        if (Frame == "")
            return this.EarthTimeDefault();

        // if no mode is set fail
        if (!this.Plugin.Config.contains(Frame + ".mode"))
            return this.EarthTimeDefault();

        switch (this.Plugin.Config.getString(Frame + ".mode").toLowerCase()) {

        case "random":
            MOTD = this.RandomKey(Frame + ".random");
            break;
        case "static":
            MOTD = this.StaticKey(Frame + ".static");
            break;
        }

        if (MOTD == "")
            MOTD = this.EarthTimeDefault();

        return MOTD;
    }

    /**
     * Loop throw the time until we find one that matches then return full key
     */
    private String EarthTimeSelect() {

        String Key = "";
        Set<String> Frames = this.Plugin.Config.getConfigurationSection("motd.earthtime.frames").getKeys(false);

        // make sure list isnt empty
        if (Frames.isEmpty())
            return Key;

        for (String Value : Frames) {

            // make sure both time values are set
            if (!this.Plugin.Config.contains("motd.earthtime.frames." + Value + ".from")
                    && !this.Plugin.Config.contains("motd.earthtime.frames." + Value + ".till"))
                return Key;

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
        if (this.Plugin.Config.contains("motd.earthtime.default"))
            return MOTD;

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
            Date TimeNow = new Date();

            // if greater, see if time is after
            if (Greater == true) {

                if (FrameTime.after(TimeNow)) {

                    return true;
                } else if (FrameTime.equals(TimeNow)) {

                    return true;
                }
            } else { // assume greater is false, run before

                if (FrameTime.before(TimeNow)) {

                    return true;
                } else if (FrameTime.equals(TimeNow)) {

                    return true;
                }
            }
        } catch (ParseException e) {

        }

        return false;
    }

    public String BuildMOTD(List<String> Values) {

        StringBuilder MOTD = new StringBuilder();
        int I = 0;

        for (String Value : Values) {

            MOTD.append(Value + (I == 0 ? "\n" : ""));
            I++;

            // dont allow more then 2 lines cause... well ya
            if (I > 1)
                break;
        }

        return MOTD.toString();
    }
}