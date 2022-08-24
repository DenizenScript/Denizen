package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.world.RaidData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;

public class RaidFinishesScriptEvent extends BukkitScriptEvent implements Listener {

    public RaidFinishesScriptEvent() {
        registerCouldMatcher("raid finishes");
    }

    public RaidFinishEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getRaid().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "winners":
                ListTag list = new ListTag();
                for (Player player : event.getWinners()) {
                    list.addObject(new PlayerTag(player));
                }
                return list;
            case "raid":
                return RaidData.toMap(event.getRaid());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidFinishes(RaidFinishEvent event) {
        this.event = event;
        fire(event);
    }
}
