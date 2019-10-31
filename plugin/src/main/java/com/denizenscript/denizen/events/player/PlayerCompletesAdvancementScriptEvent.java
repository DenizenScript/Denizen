package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerCompletesAdvancementScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player completes advancement
    //
    // @Regex ^on player completes advancement$
    //
    // @Triggers when a player has completed all criteria in an advancement.
    //
    // @Context
    // <context.criteria> returns all the criteria present in this advancement.
    //
    // @Player Always.
    //
    // -->

    public PlayerCompletesAdvancementScriptEvent() {
        instance = this;
    }

    public static PlayerCompletesAdvancementScriptEvent instance;
    public PlayerAdvancementDoneEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player completes advancement");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerCompletesAdvancement";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("criteria")) {
            ListTag criteria = new ListTag();
            criteria.addAll(event.getAdvancement().getCriteria());
            return criteria;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerCompletesAdvancement(PlayerAdvancementDoneEvent event) {
        // TODO: Should this not fire if it's a 'fake' advancement created by Denizen?
        this.event = event;
        fire(event);
    }
}
