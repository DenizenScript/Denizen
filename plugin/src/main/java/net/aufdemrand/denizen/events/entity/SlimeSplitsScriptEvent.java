package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SlimeSplitEvent;

public class SlimeSplitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // slime splits (into <#>) (in <area>)
    //
    // @Regex ^on slime splits( into [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        String s = path.event;
        String lower = path.eventLower;
        String counts = CoreUtilities.getXthArg(3, lower);

        if (CoreUtilities.xthArgEquals(2, lower, "into") && !counts.isEmpty()) {
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
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        SlimeSplitEvent.getHandlerList().unregister(this);
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
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setCount(count);
    }

}
