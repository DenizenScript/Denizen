package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AnvilBlockDamagedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // anvil block damaged|breaks
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Cancellable true
    //
    // @Switch state:<state> to only process the event if the anvil's new damage state matches the specified state.
    //
    // @Triggers when an anvil is damaged from being used.
    //
    // @Context
    // <context.state> returns an ElementTag of the anvil's new damage state. Refer to <@link url https://jd.papermc.io/paper/1.19/com/destroystokyo/paper/event/block/AnvilDamagedEvent.DamageState.html>.
    // <context.break> returns an ElementTag(Boolean) that signifies whether the anvil will break.
    // <context.inventory> returns the InventoryTag of the anvil's inventory.
    //
    // @Determine
    // "STATE:" + ElementTag to set the anvil's new damage state.
    // "BREAK:" + ElementTag(Boolean) to set weather the anvil will break.
    // -->

    public AnvilBlockDamagedScriptEvent() {
        registerCouldMatcher("anvil block damaged|breaks");
        registerSwitches("state");
    }

    public AnvilDamagedEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("breaks") && !event.isBreaking()) {
            return false;
        }
        if (!runInCheck(path, event.getInventory().getLocation())) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "state", event.getDamageState().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "state": return new ElementTag(event.getDamageState());
            case "inventory": return InventoryTag.mirrorBukkitInventory(event.getInventory());
            case "break": return new ElementTag(event.isBreaking());
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("state:")) {
                ElementTag stateElement = new ElementTag(lower.substring("state:".length()));
                if (stateElement.matchesEnum(AnvilDamagedEvent.DamageState.class)) {
                    event.setDamageState(stateElement.asEnum(AnvilDamagedEvent.DamageState.class));
                    return true;
                }
            }
            else if (lower.startsWith("break:")) {
                event.setBreaking(new ElementTag(lower.substring("break:".length())).asBoolean());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getView().getPlayer());
    }

    @EventHandler
    public void onAnvilDamaged(AnvilDamagedEvent event) {
        this.event = event;
        fire(event);
    }
}
