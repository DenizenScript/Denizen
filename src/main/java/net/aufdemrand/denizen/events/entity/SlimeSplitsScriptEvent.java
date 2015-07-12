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

import java.util.HashMap;

public class SlimeSplitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // slime splits (into <#>) (in <area>)
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String counts = CoreUtilities.getXthArg(3, lower);
        if (CoreUtilities.getXthArg(2, lower).equals("into") && counts.length() > 0) {
            try {
                if (Integer.parseInt(counts) != count) {
                    return false;
                }
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return runInCheck(scriptContainer, s, lower, entity.getLocation());
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("count", new Element(count));
        return context;
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
