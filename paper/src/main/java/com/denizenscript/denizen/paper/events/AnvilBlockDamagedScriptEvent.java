package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.inventory.InventoryViewUtil;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // "STATE:<ElementTag>" to set the anvil's new damage state.
    // "BREAK:<ElementTag(Boolean)>" to set weather the anvil will break.
    // -->

    public AnvilBlockDamagedScriptEvent() {
        registerCouldMatcher("anvil block damaged|breaks");
        registerSwitches("state");
        this.<AnvilBlockDamagedScriptEvent, ElementTag>registerOptionalDetermination("state", ElementTag.class, (evt, context, state) -> {
            if (state.matchesEnum(AnvilDamagedEvent.DamageState.class)) {
                evt.event.setDamageState(state.asEnum(AnvilDamagedEvent.DamageState.class));
                return true;
            }
            return false;
        });
        this.<AnvilBlockDamagedScriptEvent, ElementTag>registerOptionalDetermination("break", ElementTag.class, (evt, context, value) -> {
            if (value.isBoolean()) {
                evt.event.setBreaking(value.asBoolean());
                return true;
            }
            return false;
        });
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
        return switch (name) {
            case "state" -> new ElementTag(event.getDamageState());
            case "inventory" -> InventoryTag.mirrorBukkitInventory(event.getInventory());
            case "break" -> new ElementTag(event.isBreaking());
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(InventoryViewUtil.getPlayer(event.getView()));
    }

    @EventHandler
    public void onAnvilDamaged(AnvilDamagedEvent event) {
        this.event = event;
        fire(event);
    }
}
