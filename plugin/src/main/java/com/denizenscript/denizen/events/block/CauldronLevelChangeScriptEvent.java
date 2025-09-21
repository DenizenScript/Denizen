package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;

public class CauldronLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // cauldron level changes|raises|lowers
    //
    // @Group Block
    //
    // @Location true
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
    //
    // @Cancellable true
    //
    // @Triggers when a cauldron's level changes.
    //
    // @Context
    // <context.location> returns the LocationTag of the cauldron that changed.
    // <context.entity> returns the LocationTag of the entity that caused the cauldron level to change (if any).
    // <context.cause> returns the reason that the cauldron level changed, from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/CauldronLevelChangeEvent.ChangeReason.html>
    // <context.old_level> returns the previous cauldron level.
    // <context.new_level> returns the new cauldron level.
    //
    // @Determine
    // ElementTag(Number) to set the new level.
    //
    // -->

    public CauldronLevelChangeScriptEvent() {
        registerCouldMatcher("cauldron level changes|raises|lowers");
        registerSwitches("cause");
        this.<CauldronLevelChangeScriptEvent, ElementTag>registerOptionalDetermination(null, ElementTag.class, (evt, context, level) -> {
            if (!level.isInt()) {
                return false;
            }
            BlockState cauldronState = evt.event.getNewState();
            if (level.asInt() <= 0) {
                cauldronState.setType(Material.CAULDRON);
                return true;
            }
            if (level.asInt() > 3) {
                return false;
            }
            Material cauldronType = cauldronState.getType();
            if (cauldronType != Material.WATER_CAULDRON && cauldronType != Material.LAVA_CAULDRON) {
                cauldronState.setType(event.getBlock().getType());
            }
            if (cauldronState.getBlockData() instanceof Levelled levelled) {
                levelled.setLevel(level.asInt());
                cauldronState.setBlockData(levelled);
                return true;
            }
            return false;
        });
    }

    public LocationTag location;
    public CauldronLevelChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "cause", event.getReason().name())) {
            return false;
        }
        String changeType = path.eventArgLowerAt(2);
        int oldLevel = event.getBlock().getBlockData() instanceof Levelled levelled ? levelled.getLevel() : 0;
        int newLevel = event.getNewState().getBlockData() instanceof Levelled levelled ? levelled.getLevel() : 0;
        if (changeType.equals("raises")) {
            if (newLevel <= oldLevel) {
                return false;
            }
        }
        else if (changeType.equals("lowers")) {
            if (newLevel >= oldLevel) {
                return false;
            }
        }
        else if (!changeType.equals("changes")) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "cause" -> new ElementTag(event.getReason());
            case "old_level" -> new ElementTag(event.getBlock().getBlockData() instanceof Levelled levelled ? levelled.getLevel() : 0);
            case "new_level" -> new ElementTag(event.getNewState().getBlockData() instanceof Levelled levelled ? levelled.getLevel() : 0);
            case "entity" -> event.getEntity() != null ? new EntityTag(event.getEntity()).getDenizenObject() : null;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}
