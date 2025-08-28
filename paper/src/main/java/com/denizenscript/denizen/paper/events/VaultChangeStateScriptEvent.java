package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    // <context.current_state> returns the ElementTag of the vault state on change.
    // <context.new_state> returns the ElementTag of the new vault state.
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
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "current_state" -> new ElementTag(event.getCurrentState().toString().toLowerCase());
            case "new_state" -> new ElementTag(event.getNewState().toString().toLowerCase());
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
