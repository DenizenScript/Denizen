package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    // @Group Player
    //
    // @Switch name:<name> to only fire if the advancement has the specified name.
    //
    // @Triggers when a player has completed all criteria in an advancement.
    //
    // @Context
    // <context.criteria> returns all the criteria present in this advancement.
    // <context.advancement> returns the completed advancement's minecraft ID key.
    // <context.message> returns an ElementTag of the advancement message (only on Paper).
    //
    // @Determine
    // ElementTag to change the advancement message (only on Paper).
    // "NO_MESSAGE" to hide the advancement message (only on Paper).
    //
    // @Player Always.
    //
    // -->

    public PlayerCompletesAdvancementScriptEvent() {
    }

    public PlayerAdvancementDoneEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player completes advancement");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "name", event.getAdvancement().getKey().getKey())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("criteria")) {
            ListTag criteria = new ListTag();
            criteria.addAll(event.getAdvancement().getCriteria());
            return criteria;
        }
        else if (name.equals("advancement")) {
            return new ElementTag(event.getAdvancement().getKey().getKey());
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
