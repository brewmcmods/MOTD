package com.stickersthecat.brew.motd.events;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.stickersthecat.brew.motd.Main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * @TODO: Time of day MOTD
 */
public class PingEvent implements Listener {

    private Main Plugin;

    public PingEvent(Main Plugin) {

        this.Plugin = Plugin;
    }

    @EventHandler
    public void OnPing(ServerListPingEvent Event) {

        // make sure MOTD exists
        if (this.Plugin.Config.contains("motd")) {

            this.MOTD(Event);
        }
    }

    public void MOTD(ServerListPingEvent Event) {

        String MOTD = "";

        // if this doesnt exist; exit
        if (!this.Plugin.Config.contains("motd.mode"))
            return;

        switch (this.Plugin.Config.getString("motd.mode")) {

        case "static":
            MOTD = this.StaticMOTD();
            break;
        case "random":
            MOTD = this.RandomMOTD();
            break;
        default:
            break;
        }

        // if fail use static
        if (MOTD == "" && this.Plugin.Config.contains("motd.static")) {

            MOTD = this.BuildMOTD(this.Plugin.Config.getStringList("motd.static"));
        }

        // check it again and if this fails allow it to default to the server prop one
        if (MOTD != "") {

            Event.setMotd(MOTD);
        }
    }

    private String StaticMOTD() {

        return this.StaticKey("motd.static");
    }

    /**
     * Root Level random MOTD
     */
    private String RandomMOTD() {

        return this.RandomKey("motd.random");
    }

    private String EarthTimeMOTD() {

        return "";
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