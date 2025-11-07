package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.block.VaultChangeStateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VaultChangeStateScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vault changes state
    //
    // @Plugin Paper
    //
    // @Group Block
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Triggers when a vault block state changes
    //
    // @Context
    // <context.location> returns the LocationTag of the vault block.
    // <context.old_state> returns the vault state before the change.
    // <context.new_state> returns the vault state after the change.
    //
    // @Player when the entity who triggered the change is a player.
    //
    // -->

    public VaultChangeStateScriptEvent() {
        registerCouldMatcher("vault changes state");
    }

    public LocationTag location;
    public VaultChangeStateEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
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
        return switch (name) {
            case "old_state" -> new ElementTag(event.getCurrentState());
            case "new_state" -> new ElementTag(event.getNewState());
            case "location" -> location;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onVaultChangeStateEvent(VaultChangeStateEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}