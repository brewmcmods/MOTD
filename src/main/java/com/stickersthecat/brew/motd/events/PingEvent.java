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

            this.DoMOTD(Event);
        }
    }

    public void DoMOTD(ServerListPingEvent Event) {

        String MOTD = "";

        if (!this.Plugin.Config.contains("motd.mode")) {

            return;
        }

        switch (this.Plugin.Config.getString("motd.mode")) {

        case "random":
            if (!this.Plugin.Config.contains("motd.random")) {

                MOTD = this.SelectMOTD();
            }
            break;

        case "static":
            if (!this.Plugin.Config.contains("motd.static")) {

                MOTD = this.BuildMOTD(this.Plugin.Config.getStringList("motd.static"));
            }
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

    public String SelectMOTD() {

        Set<String> Set = this.Plugin.Config.getConfigurationSection("motd.random").getKeys(false);
        String[] Arr = Set.toArray(new String[Set.size()]);

        int Random = new Random().nextInt(Arr.length);

        String MOTD = "";
        if (this.Plugin.Config.contains("motd.random." + Arr[Random])) {

            MOTD = this.BuildMOTD(this.Plugin.Config.getStringList("motd.random." + Arr[Random]));
        }
        return MOTD;
    }

    public String BuildMOTD(List<String> Values) {

        StringBuilder MOTD = new StringBuilder();
        int I = 0;

        for (String Value : Values) {

            MOTD.append(Value + (I == 0 ? "\n" : ""));
            I++;
        }

        return MOTD.toString();
    }
}