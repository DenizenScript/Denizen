package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerCompletesAdvancementScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player completes advancement
    //
    // @Regex ^on player completes advancement?$
    //
    // @Cancellable false
    //
    // @Triggers when a player has completed all criteria in an advancement.
    //
    // @Context
    // <context.criteria> returns all the criteria present in this advancement.
    //
    // -->

    public PlayerCompletesAdvancementScriptEvent() {
        instance = this;
    }

    public static PlayerCompletesAdvancementScriptEvent instance;
    public dList criteria;
    public PlayerAdvancementDoneEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player completes advancement");
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("criteria")) {
            return criteria;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerCompletesAdvancement(PlayerAdvancementDoneEvent event) {
        // Should this not fire if it's a 'fake' advancement created by Denizen?
        criteria = new dList();
        criteria.addAll(event.getAdvancement().getCriteria());
        this.event = event;
        fire();
    }
}
