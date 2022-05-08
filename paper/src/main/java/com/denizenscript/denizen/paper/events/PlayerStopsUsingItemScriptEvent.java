package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;

public class PlayerStopsUsingItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player stops using <item>
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Player Always.
    //
    // @Triggers when a player stops using an item. For example: letting go when holding a bow, an edible item, or a spyglass.
    //
    // @Switch after:<duration> to only process the event if the item was being used for at least the specified duration
    //
    // @Context
    // <context.item> returns the ItemTag that was being used.
    // <context.time_used> returns the amount of time the item was used for as a DurationTag.
    //
    // -->

    public PlayerStopsUsingItemScriptEvent() {
        instance = this;
        registerCouldMatcher("player stops using <item>");
        registerSwitches("after");
    }

    public static PlayerStopsUsingItemScriptEvent instance;
    public ItemTag item;
    public PlayerStopUsingItemEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (path.switches.containsKey("after") && !DurationTag.matches(path.switches.get("after"))) {
            addPossibleCouldMatchFailReason("not a valid duration", path.switches.get("after"));
            return false;
        }
        return super.couldMatch(path);
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (!item.tryAdvancedMatcher(path.eventArgLowerAt(3))) {
            return false;
        }
        if (path.switches.containsKey("after")
                && DurationTag.valueOf(path.switches.get("after"), getTagContext(path)).getTicks() > event.getTicksHeldFor()) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerStopUsingItem";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item": return item;
            case "time_used": return new DurationTag((long) event.getTicksHeldFor());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerStopUsingItem(PlayerStopUsingItemEvent event) {
        item = new ItemTag(event.getItem());
        this.event = event;
        fire(event);
    }
}
