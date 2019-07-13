package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SlimeSplitEvent;

public class SlimeSplitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // slime splits (into <#>)
    //
    // @Regex ^on slime splits( into [^\s]+)?$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a slime splits into smaller slimes.
    //
    // @Context
    // <context.entity> returns the dEntity of the slime.
    // <context.count> returns an Element(Number) of the number of smaller slimes it will split into.
    //
    // @Determine
    // Element(Number) to set the number of smaller slimes it will split into.
    //
    // -->

    public SlimeSplitsScriptEvent() {
        instance = this;
    }

    public static SlimeSplitsScriptEvent instance;
    public dEntity entity;
    public int count;
    public SlimeSplitEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("slime splits");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String counts = path.eventArgLowerAt(3);

        if (path.eventArgLowerAt(2).equals("into") && !counts.isEmpty()) {
            try {
                if (Integer.parseInt(counts) != count) {
                    return false;
                }
            }
            catch (NumberFormatException e) {
                return false;
            }
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "SlimeSplits";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            count = aH.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("count")) {
            return new Element(count);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSlimeSplits(SlimeSplitEvent event) {
        entity = new dEntity(event.getEntity());
        count = event.getCount();
        this.event = event;
        fire(event);
        event.setCount(count);
    }

}
